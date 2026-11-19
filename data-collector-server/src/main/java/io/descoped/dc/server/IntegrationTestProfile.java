package io.descoped.dc.server;

import io.descoped.config.DynamicConfiguration;
import io.descoped.dc.api.util.CommonUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

class IntegrationTestProfile {

    static Map<String, String> loadApplicationPropertiesFromConsumerSpecificationProfile(String profile, String... replacePropertyKeyValueWithFullPath) {
        Path currentPath = CommonUtils.currentPath();
        while (!currentPath.resolve("data-collection-consumer-specifications").toFile().exists()) {
            currentPath = currentPath.getParent();
            if (currentPath == null) {
                throw new IllegalStateException("Unable to resolve 'data-collection-consumer-specifications' as parent to: " + CommonUtils.currentPath().toAbsolutePath());
            }
        }
        Path applicationPropertiesPath = currentPath.resolve("data-collection-consumer-specifications").resolve("profile").resolve(profile).resolve("conf").resolve("application.properties");
        DynamicConfiguration configuration = DynamicConfiguration.builder()
                .requiredFile(applicationPropertiesPath.toAbsolutePath())
                .build();
        Map<String, String> values = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : configuration.asMap().entrySet()) {
            Path consumerSpecificationProjectPath = currentPath.resolve("data-collection-consumer-specifications");
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
