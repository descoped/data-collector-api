package io.descoped.dc.test.server;

import io.descoped.config.DynamicConfiguration;
import io.descoped.dc.application.server.Configurations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class ConfigurationFactory {

    private final Map<TestConfigurationBinding, List<Configuration>> serverConfiguration = new ConcurrentHashMap<>();

    ConfigurationFactory() {
    }

    static ConfigurationFactory instance() {
        return ConfigurationFactorySingleton.INSTANCE;
    }

    private DynamicConfiguration.Builder createDynamicBuilder() {
        return DynamicConfiguration.builder()
                // dynamic-configuration 3 reads the first class path match only, so each defaults file has its own
                // name: the harness's own, then the server's when the server is on the class path.
                .classpath("testutils-defaults.properties")
                .classpath("application-defaults.properties")
                .classpath("application-test.properties");
    }

    private DynamicConfiguration buildDynamicBuilder(DynamicConfiguration.Builder builder) {
        return Configurations.withoutBlankValues(builder
                .systemProperties()
                .environment("DC_")
                .build());
    }

    Set<TestConfigurationBinding> keys() {
        return serverConfiguration.keySet();
    }

    /**
     * Resolve {@link DynamicConfiguration} for a {@link TestConfigurationBinding}. If binding is not found,
     * a default configuration will be returned. If test method is not found, it will try to fallback to test class.
     *
     * @param testConfigurationBinding key of className or className/methodName
     * @return Default configuration unless key matches class- or -method level configuration binding
     */
    DynamicConfiguration get(TestConfigurationBinding testConfigurationBinding) {
        DynamicConfiguration.Builder builder = createDynamicBuilder();

        List<Configuration> configurationList = serverConfiguration.getOrDefault(testConfigurationBinding, new ArrayList<>());
        for (Configuration configuration : configurationList) {
            configuration.copyTo(builder);
        }

        return buildDynamicBuilder(builder);
    }

    void putIfAbsent(TestConfigurationBinding testConfigurationBinding, String profileName) {
        List<Configuration> configurationList = serverConfiguration.computeIfAbsent(testConfigurationBinding, list -> new ArrayList<>());
        ConfigurationProfile configurationProfile = new ConfigurationProfile(profileName);
        if (!configurationList.contains(configurationProfile)) {
            configurationList.add(configurationProfile);
        }
    }

    void putIfAbsent(TestConfigurationBinding testConfigurationBinding, Map<String, String> configurationMap) {
        List<Configuration> configurationList = serverConfiguration.computeIfAbsent(testConfigurationBinding, list -> new ArrayList<>());
        ConfigurationOverride configurationOverride = new ConfigurationOverride(configurationMap);
        if (!configurationList.contains(configurationOverride)) {
            configurationList.add(configurationOverride);
        }
    }

    void clear() {
        serverConfiguration.clear();
    }

    private static class ConfigurationFactorySingleton {
        private static final ConfigurationFactory INSTANCE = new ConfigurationFactory();
    }

    abstract static class Configuration {
        abstract void copyTo(DynamicConfiguration.Builder builder);
    }

    static class ConfigurationProfile extends Configuration {
        final String profileName;

        ConfigurationProfile(String profileName) {
            this.profileName = profileName;
        }

        @Override
        void copyTo(DynamicConfiguration.Builder builder) {
            builder.classpath(String.format("application-%s.properties", profileName));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ConfigurationProfile that = (ConfigurationProfile) o;
            return profileName.equals(that.profileName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(profileName);
        }

        @Override
        public String toString() {
            return "ConfigurationProfile{" +
                    "profileName='" + profileName + '\'' +
                    '}';
        }
    }

    static class ConfigurationOverride extends Configuration {
        final Map<String, String> overrideMap;

        ConfigurationOverride(Map<String, String> overrideMap) {
            this.overrideMap = overrideMap;
        }

        @Override
        void copyTo(DynamicConfiguration.Builder builder) {
            overrideMap.forEach((key, value) -> builder.value(key, value));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ConfigurationOverride that = (ConfigurationOverride) o;
            return overrideMap.equals(that.overrideMap);
        }

        @Override
        public int hashCode() {
            return overrideMap.hashCode();
        }

        @Override
        public String toString() {
            return "ConfigurationOverride{" +
                    "overrideMap=" + overrideMap +
                    '}';
        }
    }

}
