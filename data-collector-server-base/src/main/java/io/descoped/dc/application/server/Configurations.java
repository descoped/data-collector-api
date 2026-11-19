package io.descoped.dc.application.server;

import io.descoped.config.ConfigurationException;
import io.descoped.config.ConfigurationSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;

/**
 * The configuration sources of the data collector, with the rules it has always had (dynamic-configuration 2.x).
 * dynamic-configuration 3 reads the first class path match only, lets environment variables and system properties
 * override declared keys only, and resolves {@code ${key}} references; these sources keep the earlier behaviour:
 *
 * <ul>
 *     <li>{@link #propertiesResource(String)}: every match on the class path, a later one winning, then a file with the
 *     same path;</li>
 *     <li>{@link #environment(String)}: every environment variable with the prefix, as a key without it;</li>
 *     <li>{@link #systemProperties()}: every system property.</li>
 * </ul>
 *
 * <p>The sources are {@linkplain ConfigurationSource#secret() secret} to dynamic-configuration, so their values are
 * taken literally, as before: a value that contains <code>&#36;{</code> is never resolved as a reference. They are still
 * returned by {@code asMap()} and the getters like any other value.
 */
public final class Configurations {

    private Configurations() {
    }

    /**
     * Properties from every class path resource with this path, a later one winning, then from a file with the same
     * path if it exists.
     *
     * @param resourcePath the resource path, also read as a file path
     * @return the source
     */
    public static ConfigurationSource propertiesResource(String resourcePath) {
        return source("properties " + resourcePath, () -> {
            Map<String, String> values = new LinkedHashMap<>();
            try {
                Enumeration<URL> resources = ClassLoader.getSystemResources(resourcePath);
                while (resources.hasMoreElements()) {
                    URL resource = resources.nextElement();
                    try (Reader reader = new BufferedReader(new InputStreamReader(resource.openStream(), StandardCharsets.UTF_8))) {
                        load(reader, values);
                    }
                }
                Path path = Path.of(resourcePath);
                if (Files.exists(path)) {
                    try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                        load(reader, values);
                    }
                }
            } catch (IOException e) {
                throw new ConfigurationException("Cannot read " + resourcePath, e);
            }
            return values;
        });
    }

    /**
     * Every environment variable whose name starts with the prefix, as a key without the prefix:
     * {@code DC_http.port} sets {@code http.port}.
     *
     * @param prefix the prefix
     * @return the source
     */
    public static ConfigurationSource environment(String prefix) {
        return source("environment " + prefix, () -> {
            Map<String, String> values = new LinkedHashMap<>();
            System.getenv().forEach((name, value) -> {
                if (name.startsWith(prefix)) {
                    values.put(name.substring(prefix.length()), value);
                }
            });
            return values;
        });
    }

    /**
     * Every system property.
     *
     * @return the source
     */
    public static ConfigurationSource systemProperties() {
        return source("system properties", () -> {
            Map<String, String> values = new LinkedHashMap<>();
            System.getProperties().forEach((key, value) -> values.put(String.valueOf(key), String.valueOf(value)));
            return values;
        });
    }

    /**
     * Fixed values, taken literally like the other sources.
     *
     * @param values the values
     * @return the source
     */
    public static ConfigurationSource values(Map<String, String> values) {
        Map<String, String> copy = Map.copyOf(values);
        return source("values", () -> copy);
    }

    private static void load(Reader reader, Map<String, String> values) throws IOException {
        Properties properties = new Properties();
        properties.load(reader);
        properties.forEach((key, value) -> values.put(String.valueOf(key), String.valueOf(value)));
    }

    private static ConfigurationSource source(String name, Supplier<Map<String, String>> loader) {
        return new ConfigurationSource() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public Map<String, String> load() {
                return loader.get();
            }

            @Override
            public boolean secret() {
                return true;
            }
        };
    }
}
