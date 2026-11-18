package io.descoped.dc.api.services;

import java.util.Map;

public interface Services {
    <R> boolean contains(Class<R> clazz);

    <R> R get(Class<R> clazz);

    Map<Class<?>, Object> asMap();

    <R> Services register(Class<R> clazz, Object instance);

    static Services create() {
        return new InjectionParameters();
    }
}
