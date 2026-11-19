package io.descoped.dc.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * The server's rules for configuration that comes from {@code DC_} environment variables.
 *
 * <p>dynamic-configuration 3 lets an environment variable override only a key that a properties file declares. The
 * server declares every key an operator sets in {@code application-defaults.properties}, most of them empty, and
 * treats an empty value as unset. A {@code DC_} variable that matches no declared key would be ignored; for the
 * encryption and certificate keys that would turn a setting off silently, so the server refuses to start instead.
 */
final class EnvironmentOverrides {

    static final String PREFIX = "DC_";

    private static final Logger LOG = LoggerFactory.getLogger(EnvironmentOverrides.class);

    /** Unmatched variables with these prefixes (after DC_, normalized) stop the server. */
    private static final List<String> FAIL_CLOSED = List.of("RAWDATA_ENCRYPTION_", "DATA_COLLECTOR_SSLBUNDLE_");

    /** Variables the image sets for itself, not configuration. */
    private static final Set<String> NOT_CONFIGURATION = Set.of("DC_HOME");

    private EnvironmentOverrides() {
    }

    /**
     * Checks that every {@code DC_} variable overrides a declared key.
     *
     * @param environment  the environment variables
     * @param declaredKeys the keys the configuration declares
     * @throws IllegalStateException if an unmatched variable belongs to the encryption or certificate settings
     */
    static void requireMatched(Map<String, String> environment, Set<String> declaredKeys) {
        Set<String> names = new TreeSet<>();
        for (String key : declaredKeys) {
            names.add(PREFIX + key);
            String normalized = PREFIX + key.replaceAll("[^A-Za-z0-9]", "_");
            names.add(normalized);
            names.add(normalized.toUpperCase(java.util.Locale.ROOT));
        }
        Set<String> failClosed = new TreeSet<>();
        for (String variable : environment.keySet()) {
            if (!variable.startsWith(PREFIX) || NOT_CONFIGURATION.contains(variable) || names.contains(variable)) {
                continue;
            }
            String normalized = variable.substring(PREFIX.length()).replaceAll("[^A-Za-z0-9]", "_").toUpperCase(java.util.Locale.ROOT);
            if (FAIL_CLOSED.stream().anyMatch(normalized::startsWith)) {
                failClosed.add(variable);
            } else {
                LOG.warn("Environment variable {} matches no configuration key and is ignored", variable);
            }
        }
        if (!failClosed.isEmpty()) {
            throw new IllegalStateException("Environment variables match no configuration key, so the server would "
                    + "ignore them: " + failClosed + ". Declare the keys in /conf/application.properties.");
        }
    }
}
