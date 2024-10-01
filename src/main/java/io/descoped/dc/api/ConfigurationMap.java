package io.descoped.dc.api;

import java.util.Map;

public class ConfigurationMap {

    private final Map<String, String> configMap;

    public ConfigurationMap(Map<String, String> configMap) {
        this.configMap = configMap;
    }

    public boolean contains(String key) {
        return configMap.containsKey(key);
    }

    public String get(String key) {
        return configMap.get(key);
    }

    public Map<String, String> asMap() {
        return configMap;
    }

    public void put(String key, String value) {
        configMap.put(key, value);
    }
}
