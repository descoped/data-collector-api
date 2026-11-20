package io.descoped.dc.server;

import io.descoped.config.DynamicConfiguration;
import io.descoped.dc.api.util.CommonUtils;
import io.descoped.dc.application.server.Configurations;

import java.nio.file.Path;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

class IntegrationTestProfile {

    private static final String EXAMPLES = "examples";

    static Map<String, String> loadApplicationPropertiesFromConsumerSpecificationProfile(String profile, String... replacePropertyKeyValueWithFullPath) {
        Path currentPath = CommonUtils.currentPath();
        while (!currentPath.resolve(EXAMPLES).toFile().exists()) {
            currentPath = currentPath.getParent();
            if (currentPath == null) {
                throw new IllegalStateException("Unable to resolve '" + EXAMPLES + "' as parent to: " + CommonUtils.currentPath().toAbsolutePath());
            }
        }
        Path applicationPropertiesPath = currentPath.resolve(EXAMPLES).resolve("profile").resolve(profile).resolve("conf").resolve("application.properties");
        DynamicConfiguration configuration = DynamicConfiguration.builder()
                .source(Configurations.propertiesResource(applicationPropertiesPath.toAbsolutePath().toString()))
                .build();
        Map<String, String> values = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : configuration.asMap().entrySet()) {
            Path consumerSpecificationProjectPath = currentPath.resolve(EXAMPLES);
            List<String> prefixFullPathForPropertyNames = List.of(replacePropertyKeyValueWithFullPath);
            if (prefixFullPathForPropertyNames.stream().anyMatch(name -> name.equals(entry.getKey()))) {
                values.put(entry.getKey(), consumerSpecificationProjectPath.toAbsolutePath().normalize().toString() + entry.getValue());
            } else {
                values.put(entry.getKey(), entry.getValue());
            }
        }
        return values;
    }
}
