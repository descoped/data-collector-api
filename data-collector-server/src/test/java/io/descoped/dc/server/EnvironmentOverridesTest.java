package io.descoped.dc.server;

import io.descoped.config.DynamicConfiguration;
import io.descoped.dc.application.server.Configurations;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EnvironmentOverridesTest {

    @Test
    void thatEmptyValuesReadAsUnset() {
        DynamicConfiguration configuration = Configurations.withoutBlankValues(DynamicConfiguration.builder()
                .value("rawdata.encryption.key", "")
                .value("http.port", "9990")
                .build());
        assertTrue(configuration.find("rawdata.encryption.key").isEmpty());
        assertEquals(9990, configuration.getInt("http.port"));
    }

    @Test
    void thatVariablesForDeclaredKeysPass() {
        assertDoesNotThrow(() -> EnvironmentOverrides.requireMatched(Map.of(
                "DC_RAWDATA_ENCRYPTION_KEY", "x",
                "DC_rawdata.encryption.salt", "y",
                "DC_HOME", "/opt/dc",
                "PATH", "/bin"), Set.of("rawdata.encryption.key", "rawdata.encryption.salt")));
    }

    @Test
    void thatUndeclaredEncryptionAndCertificateVariablesStopTheServer() {
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> EnvironmentOverrides.requireMatched(Map.of(
                "DC_RAWDATA_ENCRYPTION_KEY", "x",
                "DC_DATA_COLLECTOR_SSLBUNDLE_PROVIDER", "google-secret-manager"), Set.of("http.port")));
        assertTrue(e.getMessage().contains("DC_RAWDATA_ENCRYPTION_KEY"), e.getMessage());
        assertTrue(e.getMessage().contains("DC_DATA_COLLECTOR_SSLBUNDLE_PROVIDER"), e.getMessage());
        assertTrue(!e.getMessage().contains("google-secret-manager"), "values never reach the message");
    }

    @Test
    void thatOtherUndeclaredVariablesOnlyWarn() {
        assertDoesNotThrow(() -> EnvironmentOverrides.requireMatched(Map.of("DC_SOMETHING_ELSE", "x"), Set.of()));
    }

    @Test
    void thatTheShippedDefaultsDeclareTheEncryptionKeys() {
        DynamicConfiguration defaults = DynamicConfiguration.builder().classpath("application-defaults.properties").build();
        assertDoesNotThrow(() -> EnvironmentOverrides.requireMatched(Map.of(
                "DC_RAWDATA_ENCRYPTION_PROVIDER", "google-secret-manager",
                "DC_RAWDATA_ENCRYPTION_KEY", "k",
                "DC_RAWDATA_ENCRYPTION_SALT", "s",
                "DC_DATA_COLLECTOR_SSLBUNDLE_PASSPHRASE", "p"), defaults.asMap().keySet()));
    }
}
