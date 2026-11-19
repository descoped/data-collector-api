package io.descoped.dc.content.provider.rawdata;

import io.descoped.dc.api.content.ContentStore;
import io.descoped.dc.api.content.ContentStoreInitializer;
import io.descoped.rawdata.api.RawdataClient;
import io.descoped.secrets.api.SecretManagerClient;
import io.descoped.service.provider.api.ProviderName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@ProviderName("rawdata")
public class RawdataClientContentStoreInitializer implements ContentStoreInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(RawdataClientContentStoreInitializer.class);

    @Override
    public Set<String> configurationKeys() {
        return Set.of("rawdata.client.provider");
    }

    /*
     * Encryption of content, optional:
     *
     * rawdata.encryption.provider = dynamic-configuration | google-secret-manager; without it, the key and salt are
     *                                read from this configuration
     * rawdata.encryption.key      = the password (dynamic-configuration, or this configuration), or the name of the
     *                                secret that holds it (google-secret-manager)
     * rawdata.encryption.salt     = the salt, at least 16 bytes, or the name of the secret that holds it
     * rawdata.encryption.gcp.serviceAccountKeyPath = dynamic-configuration: a properties file with the two secrets;
     *                                google-secret-manager: a service-account key (Application Default Credentials
     *                                when absent)
     * rawdata.encryption.gcp.projectId = google-secret-manager: the project
     */

    @Override
    public ContentStore initialize(Map<String, String> configuration) {
        LOG.info("Rawdata client provider: {}", configuration.get(RawdataClient.PROVIDER_KEY));

        String encryptionProvider = configuration.get("rawdata.encryption.provider");
        boolean ignoreEncryption = Boolean.parseBoolean(configuration.get("recovery.rawdata.encryption.credentials.ignore"));
        Map<String, String> secretsConfiguration = ignoreEncryption ? Map.of() : secretsConfiguration(encryptionProvider, configuration);
        LOG.info("Rawdata encryption enabled: {}", !secretsConfiguration.isEmpty());

        if (secretsConfiguration.isEmpty()) {
            return new RawdataClientContentStore(RawdataClient.create(configuration), null, null);
        }

        boolean google = "google-secret-manager".equals(encryptionProvider);
        String keySecretName = google ? required(configuration, "rawdata.encryption.key") : "rawdata.encryption.key";
        String saltSecretName = google ? required(configuration, "rawdata.encryption.salt") : "rawdata.encryption.salt";
        char[] encryptionKey = null;
        byte[] encryptionSalt = null;
        try (SecretManagerClient secretManagerClient = SecretManagerClient.create(secretsConfiguration)) {
            encryptionKey = secretManagerClient.readChars(keySecretName);
            encryptionSalt = secretManagerClient.readBytes(saltSecretName);
            return new RawdataClientContentStore(RawdataClient.create(configuration), encryptionKey, encryptionSalt);
        } finally {
            if (encryptionKey != null) Arrays.fill(encryptionKey, '\u0000');
            if (encryptionSalt != null) Arrays.fill(encryptionSalt, (byte) 0);
        }
    }

    private static Map<String, String> secretsConfiguration(String encryptionProvider, Map<String, String> configuration) {
        Map<String, String> secrets = new LinkedHashMap<>();
        String keyPath = configuration.get("rawdata.encryption.gcp.serviceAccountKeyPath");
        if ("dynamic-configuration".equals(encryptionProvider)) {
            secrets.put("secrets.provider", encryptionProvider);
            if (keyPath != null) {
                secrets.put("secrets.property-resource-path", keyPath);
            } else {
                putInline(secrets, configuration);
            }
        } else if ("google-secret-manager".equals(encryptionProvider)) {
            secrets.put("secrets.provider", encryptionProvider);
            secrets.put("secrets.project-id", required(configuration, "rawdata.encryption.gcp.projectId"));
            if (keyPath != null) {
                secrets.put("secrets.service-account-key-path", keyPath);
            }
        } else if (encryptionProvider != null) {
            throw new IllegalArgumentException("Unknown rawdata.encryption.provider: " + encryptionProvider
                    + " (dynamic-configuration or google-secret-manager)");
        } else if (configuration.containsKey("rawdata.encryption.key") && configuration.containsKey("rawdata.encryption.salt")) {
            secrets.put("secrets.provider", "dynamic-configuration");
            putInline(secrets, configuration);
        }
        return secrets;
    }

    private static void putInline(Map<String, String> secrets, Map<String, String> configuration) {
        secrets.put("secrets.values.rawdata.encryption.key", required(configuration, "rawdata.encryption.key"));
        secrets.put("secrets.values.rawdata.encryption.salt", required(configuration, "rawdata.encryption.salt"));
    }

    private static String required(Map<String, String> configuration, String key) {
        String value = configuration.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing configuration: " + key);
        }
        return value;
    }
}
