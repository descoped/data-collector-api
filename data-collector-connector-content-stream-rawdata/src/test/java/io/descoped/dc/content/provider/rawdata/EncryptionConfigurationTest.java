package io.descoped.dc.content.provider.rawdata;

import io.descoped.rawdata.api.RawdataClient;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.descoped.dc.content.provider.rawdata.RawdataClientContentStoreInitializer.secretsConfiguration;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EncryptionConfigurationTest {

    @Test
    void thatNoKeyAndNoSaltMeansNoEncryption() {
        assertTrue(secretsConfiguration(null, Map.of()).isEmpty());
        assertTrue(secretsConfiguration(null, Map.of("rawdata.encryption.key", " ", "rawdata.encryption.salt", "")).isEmpty());
    }

    @Test
    void thatKeyAndSaltFromTheConfigurationAreInlineSecrets() {
        Map<String, String> secrets = secretsConfiguration(null, Map.of(
                "rawdata.encryption.key", "password", "rawdata.encryption.salt", "a-salt-of-16-bytes"));
        assertEquals(Map.of(
                "secrets.provider", "dynamic-configuration",
                "secrets.values.rawdata.encryption.key", "password",
                "secrets.values.rawdata.encryption.salt", "a-salt-of-16-bytes"), secrets);
    }

    @Test
    void thatAKeyWithoutASaltFailsClosed() {
        assertThrows(IllegalArgumentException.class, () -> secretsConfiguration(null, Map.of("rawdata.encryption.key", "password")));
        assertThrows(IllegalArgumentException.class, () -> secretsConfiguration(null, Map.of("rawdata.encryption.salt", "a-salt-of-16-bytes")));
    }

    @Test
    void thatDynamicConfigurationReadsAPropertiesFile() {
        Map<String, String> secrets = secretsConfiguration("dynamic-configuration", Map.of(
                "rawdata.encryption.gcp.serviceAccountKeyPath", "/secrets/rawdata.properties"));
        assertEquals(Map.of(
                "secrets.provider", "dynamic-configuration",
                "secrets.property-resource-path", "/secrets/rawdata.properties"), secrets);
    }

    @Test
    void thatGoogleSecretManagerNeedsAProject() {
        assertThrows(IllegalArgumentException.class, () -> secretsConfiguration("google-secret-manager", Map.of()));
        Map<String, String> secrets = secretsConfiguration("google-secret-manager", Map.of(
                "rawdata.encryption.gcp.projectId", "my-project",
                "rawdata.encryption.gcp.serviceAccountKeyPath", "/secrets/sa.json"));
        assertEquals(Map.of(
                "secrets.provider", "google-secret-manager",
                "secrets.project-id", "my-project",
                "secrets.service-account-key-path", "/secrets/sa.json"), secrets);
    }

    @Test
    void thatAnUnknownProviderFailsClosed() {
        assertThrows(IllegalArgumentException.class, () -> secretsConfiguration("dynamic-secret-configuration", Map.of()));
    }

    @Test
    void thatAnEmptyKeyOrSaltFailsClosed() {
        RawdataClient client = RawdataClient.create(Map.of("rawdata.client.provider", "memory"));
        assertThrows(IllegalArgumentException.class, () -> new RawdataClientContentStore(client, new char[0], "a-salt-of-16-bytes".getBytes()));
        assertThrows(IllegalArgumentException.class, () -> new RawdataClientContentStore(client, "password".toCharArray(), null));
    }
}
