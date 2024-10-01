package io.descoped.dc.api.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class InjectionParameters implements Services {

    private final Map<Class<?>, AtomicReference<Object>> instanceMap = new ConcurrentHashMap<>();

    public InjectionParameters() {
    }

    @Override
    public <R> boolean contains(Class<R> clazz) {
        return instanceMap.containsKey(clazz);
    }

    @Override
    public <R> R get(Class<R> clazz) {
        try {
            return (R) instanceMap.get(clazz).get();
        } catch (NullPointerException npe) {
            return null;
        }
    }

    @Override
    public Map<Class<?>, Object> asMap() {
        return instanceMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, value -> value.getValue().get()));
    }

    @Override
    public <R> Services register(Class<R> clazz, Object instance) {
        instanceMap.put(clazz, new AtomicReference<>(instance));
        return this;
    }

    public void putAll(InjectionParameters injectionParameters) {
        for (Map.Entry<Class<?>, AtomicReference<Object>> entry : injectionParameters.instanceMap.entrySet()) {
            instanceMap.put(entry.getKey(), entry.getValue());
        }
    }

}
