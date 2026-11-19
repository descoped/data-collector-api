package io.descoped.dc.application.server;

import io.descoped.config.DynamicConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configuration rules shared by the server and its test harness.
 */
public final class Configurations {

    private Configurations() {
    }

    /**
     * Returns the configuration without its empty values. The server declares the keys an operator may set empty in
     * its defaults, because dynamic-configuration 3 lets environment variables and system properties override only
     * declared keys; an empty value reads as unset.
     *
     * @param configuration the configuration
     * @return the same keys and values, without the empty ones
     */
    public static DynamicConfiguration withoutBlankValues(DynamicConfiguration configuration) {
        Map<String, String> values = new LinkedHashMap<>();
        configuration.asMap().forEach((key, value) -> {
            if (value != null && !value.isBlank()) {
                values.put(key, value);
            }
        });
        return DynamicConfiguration.builder().values(values).build();
    }
}
