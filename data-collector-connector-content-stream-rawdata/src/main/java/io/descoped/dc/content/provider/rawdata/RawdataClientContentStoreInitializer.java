package io.descoped.dc.content.provider.rawdata;

import io.descoped.dc.api.content.ContentStore;
import io.descoped.dc.api.content.ContentStoreInitializer;
import io.descoped.rawdata.api.RawdataClient;
import io.descoped.secrets.api.SecretManagerClient;
import io.descoped.secrets.api.SecretNotFoundException;
import io.descoped.service.provider.api.ProviderName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@ProviderName("rawdata")
public class RawdataClientContentStoreInitializer implements ContentStoreInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(RawdataClientContentStoreInitializer.class);

    @Override
    public Set<String> configurationKeys() {
        return Set.of("rawdata.client.provider");
    }

    /*
     * Encryption of content, optional (the rules of 2.x):
     *
     * rawdata.encryption.provider = dynamic-secret-configuration | google-secret-manager (dynamic-configuration is
     *                                accepted for dynamic-secret-configuration, the provider's name in secrets-client 3)
     * rawdata.encryption.gcp.projectId => google-secret-manager
     * rawdata.encryption.gcp.serviceAccountKeyPath => google-secret-manager: a service-account key;
     *                                dynamic-secret-configuration: a properties file holding the key and salt
     * rawdata.encryption.key  = the password, or with google-secret-manager the name of its secret
     * rawdata.encryption.salt = the salt, or with google-secret-manager the name of its secret
     *
     * Without a provider, the key and salt are read from this configuration when both are set. Content is encrypted
     * only when both a key and a salt are found and not empty.
     */

    @Override
    public ContentStore initialize(Map<String, String> configuration) {
        LOG.info("Content stream connector: {}", configuration.get("content.stream.connector"));

        LOG.info("Rawdata client provider: {}", configuration.get("rawdata.client.provider"));
        LOG.info("Rawdata encryption enabled: {}", configuration.containsKey("rawdata.encryption.key"));

        String encryptionProvider = configuration.get("rawdata.encryption.provider");
        Map<String, String> secretsConfiguration = secretsConfiguration(encryptionProvider, configuration);

        if (secretsConfiguration.isEmpty()) {
            return new RawdataClientContentStore(RawdataClient.create(configuration), null, null);
        }

        // Set in RecoveryContentStoreComponent
        if (Boolean.parseBoolean(configuration.get("recovery.rawdata.encryption.credentials.ignore"))) {
            return new RawdataClientContentStore(RawdataClient.create(configuration), null, null);
        }

        LOG.debug("Load encryption credentials from: {}", encryptionProvider == null ? "application configuration" : encryptionProvider);
        boolean google = GOOGLE.equals(encryptionProvider);
        String keySecretName = google ? Optional.ofNullable(configuration.get("rawdata.encryption.key")).orElseThrow() : "rawdata.encryption.key";
        String saltSecretName = google ? Optional.ofNullable(configuration.get("rawdata.encryption.salt")).orElseThrow() : "rawdata.encryption.salt";
        char[] encryptionKey = null;
        byte[] encryptionSalt = null;
        try (SecretManagerClient secretManagerClient = SecretManagerClient.create(secretsConfiguration)) {
            // secrets-client 2 returned null for a missing secret; 3 throws
            encryptionKey = readOrNull(() -> secretManagerClient.readChars(keySecretName));
            encryptionSalt = readOrNull(() -> secretManagerClient.readBytes(saltSecretName));
            return new RawdataClientContentStore(RawdataClient.create(configuration), encryptionKey, encryptionSalt);
        } finally {
            if (encryptionKey != null) Arrays.fill(encryptionKey, '\u0000');
            if (encryptionSalt != null) Arrays.fill(encryptionSalt, (byte) 0);
        }
    }

    private static final String DYNAMIC = "dynamic-secret-configuration";
    private static final String GOOGLE = "google-secret-manager";

    static Map<String, String> secretsConfiguration(String encryptionProvider, Map<String, String> configuration) {
        Map<String, String> secrets = new LinkedHashMap<>();
        String keyPath = configuration.get("rawdata.encryption.gcp.serviceAccountKeyPath");
        if (DYNAMIC.equals(encryptionProvider) || "dynamic-configuration".equals(encryptionProvider)) {
            // secrets-client 3 names this provider dynamic-configuration
            secrets.put("secrets.provider", "dynamic-configuration");
            if (keyPath == null) {
                LOG.info("Use application configuration for encryption credentials");
                putInline(secrets, configuration);
            } else {
                LOG.info("Use custom encryption credentials configuration: {}", keyPath);
                secrets.put("secrets.property-resource-path", keyPath);
            }
        } else if (GOOGLE.equals(encryptionProvider)) {
            LOG.info("Use Google Secret Manager for encryption credentials");
            secrets.put("secrets.provider", encryptionProvider);
            secrets.put("secrets.project-id", Optional.ofNullable(configuration.get("rawdata.encryption.gcp.projectId")).orElseThrow());
            // Application Default Credentials (2.x: Compute Engine) when no service account key is set
            if (keyPath != null) {
                secrets.put("secrets.service-account-key-path", keyPath);
            }
        } else if (configuration.containsKey("rawdata.encryption.key") && configuration.containsKey("rawdata.encryption.salt")) {
            LOG.info("Fallback application configuration for encryption credentials");
            secrets.put("secrets.provider", "dynamic-configuration");
            putInline(secrets, configuration);
        }
        return secrets;
    }

    // The provider reads the secrets it is given; with 2.x it read them from the whole application configuration.
    // Only these two are ever read.
    private static void putInline(Map<String, String> secrets, Map<String, String> configuration) {
        for (String key : new String[]{"rawdata.encryption.key", "rawdata.encryption.salt"}) {
            String value = configuration.get(key);
            if (value != null) {
                secrets.put("secrets.values." + key, value);
            }
        }
    }

    private static <T> T readOrNull(java.util.function.Supplier<T> read) {
        try {
            return read.get();
        } catch (SecretNotFoundException e) {
            return null;
        }
    }
}
