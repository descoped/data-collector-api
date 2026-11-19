package io.descoped.dc.test.server;

import io.descoped.config.DynamicConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConfigurationFactoryTest {

    @Test
    void testConfigurationKey() {
        TestConfigurationBinding classKey = TestConfigurationBinding.clazz("ClassA");
        assertEquals("ClassA", classKey.getClassName());

        TestConfigurationBinding methodKey = TestConfigurationBinding.method("ClassA", "method");
        assertEquals("ClassA", methodKey.getClassName());
        assertEquals("method", methodKey.getMethodName().orElseThrow());
    }

    @Test
    void thatConfigurationKeyComparesEqualTo() {
        TestConfigurationBinding defaultExpected = TestConfigurationBinding.container();
        assertEquals(defaultExpected, TestConfigurationBinding.container());
    }

    @Test
    void thatKeysAreNotDuplicated() {
        ConfigurationFactory factory = new ConfigurationFactory();

        factory.putIfAbsent(TestConfigurationBinding.clazz("ClassA"), "profile1");
        assertEquals(1, factory.keys().size());

        factory.putIfAbsent(TestConfigurationBinding.clazz("ClassA"), "profile1");
        assertEquals(1, factory.keys().size());

        factory.putIfAbsent(TestConfigurationBinding.method("ClassA", "method1"), "profile2");
        assertEquals(2, factory.keys().size());

        factory.putIfAbsent(TestConfigurationBinding.method("ClassA", "method1"), "profile2");
        assertEquals(2, factory.keys().size());

        factory.clear();
        assertEquals(0, factory.keys().size());
    }

    @Test
    void testConfigurationProfileIsLoaded() {
        DynamicConfiguration.Builder builder = DynamicConfiguration.builder();

        ConfigurationFactory.ConfigurationProfile configurationProfile = new ConfigurationFactory.ConfigurationProfile("dummy");
        configurationProfile.copyTo(builder);

        DynamicConfiguration configuration = builder.build();

        assertEquals("v1", configuration.find("k1").orElse(null));

        assertEquals(configurationProfile, new ConfigurationFactory.ConfigurationProfile("dummy"));
        assertEquals(configurationProfile.hashCode(), Objects.hash("dummy"));
        assertNotNull(configurationProfile.toString());
    }

    @Test
    void testConfigurationOverrideIsLoaded() {
        DynamicConfiguration.Builder builder = DynamicConfiguration.builder();

        Map<String, String> map = Map.of("k1", "v1");
        ConfigurationFactory.ConfigurationOverride configurationOverride = new ConfigurationFactory.ConfigurationOverride(map);
        configurationOverride.copyTo(builder);

        DynamicConfiguration configuration = builder.build();

        assertEquals("v1", configuration.find("k1").orElse(null));

        assertEquals(configurationOverride, new ConfigurationFactory.ConfigurationOverride(map));
        assertEquals(configurationOverride.hashCode(), map.hashCode());
        assertNotNull(configurationOverride.toString());
    }

    @Test
    void testConfigurationProfileAndOverrideIsLoaded() {
        DynamicConfiguration.Builder builder = DynamicConfiguration.builder();

        ConfigurationFactory.ConfigurationProfile configurationProfile = new ConfigurationFactory.ConfigurationProfile("dummy");
        configurationProfile.copyTo(builder);

        Map<String, String> map = Map.of("k2", "v2");
        ConfigurationFactory.ConfigurationOverride configurationOverride = new ConfigurationFactory.ConfigurationOverride(map);
        configurationOverride.copyTo(builder);

        DynamicConfiguration configuration = builder.build();

        assertEquals("v1", configuration.find("k1").orElse(null));
        assertEquals("v2", configuration.find("k2").orElse(null));

        assertEquals(configurationOverride, new ConfigurationFactory.ConfigurationOverride(map));
        assertEquals(configurationOverride.hashCode(), map.hashCode());
        assertNotNull(configurationOverride.toString());
    }

}
