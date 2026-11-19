package io.descoped.dc.application.server;

import io.descoped.config.DynamicConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfigurationsTest {

    static final Map<String, String> VALUES = Map.of(
            "a", "${b}",
            "b", "plain",
            "c", "x$${y}",
            "d", "$$$${z}$",
            "e", "${unterminated");

    @Test
    void thatValuesAreTakenLiterally() {
        DynamicConfiguration configuration = DynamicConfiguration.builder()
                .source(Configurations.values(VALUES))
                .build();
        assertEquals(VALUES, configuration.asMap());
    }

    @Test
    void thatValuesAreNotMasked() {
        String description = DynamicConfiguration.builder()
                .source(Configurations.values(Map.of("http.cors.allow.origin", ".*", "a", "${b}")))
                .build()
                .describe();
        assertTrue(description.contains("http.cors.allow.origin = .*"), description);
        assertTrue(description.contains("a = ${b}"), description);
        assertFalse(description.contains("****"), description);
    }

    @Test
    void thatKeysNamedLikeSecretsStayMasked() {
        // dynamic-configuration masks keys named like secrets (password, secret, token, credential, ...) whatever the source
        String description = DynamicConfiguration.builder()
                .source(Configurations.values(Map.of("http.cors.allow.credentials", "true")))
                .build()
                .describe();
        assertTrue(description.contains("http.cors.allow.credentials = ****"), description);
    }

    @Test
    void thatALaterSourceStillWins() {
        DynamicConfiguration configuration = DynamicConfiguration.builder()
                .source(Configurations.values(Map.of("k", "first")))
                .source(Configurations.values(Map.of("k", "${second}")))
                .build();
        assertEquals("${second}", configuration.get("k"));
    }
}
