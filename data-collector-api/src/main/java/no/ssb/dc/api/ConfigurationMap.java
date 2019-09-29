package no.ssb.dc.api;

import java.util.Map;

public class ConfigurationMap {

    private final Map<String, String> configMap;

    public ConfigurationMap(Map<String, String> configMap) {
        this.configMap = configMap;
    }

    public String get(String key) {
        return configMap.get(key);
    }

}
