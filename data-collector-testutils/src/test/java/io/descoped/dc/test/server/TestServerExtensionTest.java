package io.descoped.dc.test.server;

import io.descoped.config.DynamicConfiguration;
import io.descoped.dc.test.client.TestClient;
import io.descoped.dc.test.config.ConfigurationOverride;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(TestServerExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class TestServerExtensionTest {

    @Inject
    TestClient testClient;

    @Inject
    TestServer testServer;

    @Test
    void testDefaultMethodConfiguration() {
        DynamicConfiguration defaultConfiguration = ConfigurationFactory.instance().get(TestConfigurationBinding.clazz(TestServerExtensionTest.class));
        assertNotNull(defaultConfiguration);
        assertNull(defaultConfiguration.find("k1").orElse(null));
        assertConfiguration("testDefaultMethodConfiguration");
    }

    @ConfigurationOverride({"k1", "v1"})
    @Test
    void testMethodConfigurationOverride1() throws Exception {
        assertNotNull(testClient);
        assertNotNull(testServer);
        assertConfiguration("testMethodConfigurationOverride1", "k1", "v1");
    }

    @ConfigurationOverride({"k2", "v2"})
    @Test
    void testMethodConfigurationOverride2() throws Exception {
        assertNotNull(testClient);
        assertNotNull(testServer);
        assertConfiguration("testMethodConfigurationOverride2", "k2", "v2");
    }

    @Test
    void testTestServerAndClientParameters(TestServer server, TestClient client) {
        assertEquals(testServer, server);
        assertEquals(testClient, client);
    }

    private void assertConfiguration(String testMethodName, String... keyAndValuePairs) {
        DynamicConfiguration testMethodConfiguration = ConfigurationFactory.instance().get(TestConfigurationBinding.method(TestServerExtensionTest.class, testMethodName));
        assertNotNull(testMethodConfiguration);
        List<String> keyAndValueList = List.of(keyAndValuePairs);
        assertEquals(0, keyAndValueList.size() % 2, "KeyValue pairs are not even!");
        IntStream.range(0, keyAndValueList.size())
                .filter(i -> i % 2 == 0)
                .forEach(n -> assertEquals(keyAndValueList.get(n + 1), testMethodConfiguration.find(keyAndValueList.get(n)).orElse(null)));
        // Both include every system property, and the JDK sets some while it runs (sun.nio.ch.bugLevel); compare the rest.
        assertEquals(withoutSystemProperties(testServer.getConfiguration().asMap()), withoutSystemProperties(testMethodConfiguration.asMap()));
    }

    private static java.util.Map<String, String> withoutSystemProperties(java.util.Map<String, String> configuration) {
        java.util.Map<String, String> result = new java.util.TreeMap<>(configuration);
        result.keySet().removeAll(System.getProperties().stringPropertyNames());
        return result;
    }
}
