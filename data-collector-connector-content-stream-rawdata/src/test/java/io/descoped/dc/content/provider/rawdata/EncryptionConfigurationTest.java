package io.descoped.dc.content.provider.rawdata;

import io.descoped.encryption.client.Algorithm;
import io.descoped.encryption.client.EncryptionClient;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static io.descoped.dc.content.provider.rawdata.RawdataClientContentStoreInitializer.secretsConfiguration;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The encryption configuration keeps the rules of 2.x on secrets-client 3 and encryption-util 3.
 */
public class EncryptionConfigurationTest {

    @Test
    void thatNoProviderAndNoKeyAndSaltMeansNoEncryption() {
        assertTrue(secretsConfiguration(null, Map.of()).isEmpty());
        assertTrue(secretsConfiguration(null, Map.of("rawdata.encryption.key", "password")).isEmpty());
    }

    @Test
    void thatKeyAndSaltFromTheConfigurationAreInlineSecrets() {
        assertEquals(Map.of(
                "secrets.provider", "dynamic-configuration",
                "secrets.values.rawdata.encryption.key", "password",
                "secrets.values.rawdata.encryption.salt", "salt"), secretsConfiguration(null, Map.of(
                "rawdata.encryption.key", "password", "rawdata.encryption.salt", "salt")));
    }

    @Test
    void thatTheDynamicProviderKeepsIts2xName() {
        Map<String, String> configuration = Map.of("rawdata.encryption.key", "password", "rawdata.encryption.salt", "salt");
        Map<String, String> expected = Map.of(
                "secrets.provider", "dynamic-configuration",
                "secrets.values.rawdata.encryption.key", "password",
                "secrets.values.rawdata.encryption.salt", "salt");
        assertEquals(expected, secretsConfiguration("dynamic-secret-configuration", configuration));
        assertEquals(expected, secretsConfiguration("dynamic-configuration", configuration));
        assertEquals(Map.of(
                "secrets.provider", "dynamic-configuration",
                "secrets.property-resource-path", "/secrets/rawdata.properties"), secretsConfiguration("dynamic-secret-configuration", Map.of(
                "rawdata.encryption.gcp.serviceAccountKeyPath", "/secrets/rawdata.properties")));
    }

    @Test
    void thatGoogleSecretManagerUsesTheNewKeyNames() {
        assertEquals(Map.of(
                "secrets.provider", "google-secret-manager",
                "secrets.project-id", "my-project",
                "secrets.service-account-key-path", "/secrets/sa.json"), secretsConfiguration("google-secret-manager", Map.of(
                "rawdata.encryption.gcp.projectId", "my-project",
                "rawdata.encryption.gcp.serviceAccountKeyPath", "/secrets/sa.json")));
        assertThrows(RuntimeException.class, () -> secretsConfiguration("google-secret-manager", Map.of()));
    }

    @Test
    void thatTheKeyIsDerivedAs2xDid() {
        EncryptionClient client = new EncryptionClient(Algorithm.AES256);
        char[] password = "password".toCharArray();
        byte[] salt = "a-salt-of-16-bytes".getBytes(StandardCharsets.UTF_8);
        SecretKey derived = RawdataClientContentStore.deriveKey(client, password, salt);
        SecretKey legacy = client.deriveKey(password, salt, EncryptionClient.LEGACY_PBKDF2_ITERATIONS);
        assertArrayEquals(legacy.getEncoded(), derived.getEncoded());
        byte[] plaintext = "content".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(plaintext, client.decrypt(legacy, client.encrypt(derived, plaintext)));
    }

    @Test
    void thatAShortSaltStillWorks() {
        EncryptionClient client = new EncryptionClient(Algorithm.AES256);
        assertNotNull(RawdataClientContentStore.deriveKey(client, "password".toCharArray(), "salt".getBytes(StandardCharsets.UTF_8)));
    }
}
