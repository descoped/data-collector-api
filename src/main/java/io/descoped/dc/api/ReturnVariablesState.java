package io.descoped.dc.api;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ReturnVariablesState {

    private final Map<String, Object> variableMap = new LinkedHashMap<>();

    public ReturnVariablesState() {
    }

    public void add(String name) {
        variableMap.put(name, null);
    }

    public void add(String name, Object value) {
        variableMap.put(name, value);
    }

    public Set<String> keySet() {
        return variableMap.keySet();
    }

    public Object value(String key) {
        return variableMap.get(key);
    }

    public String asString() {
        return variableMap.entrySet().stream().map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue())).collect(Collectors.joining(","));
    }
}
