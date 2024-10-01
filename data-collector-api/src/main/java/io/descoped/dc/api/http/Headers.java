package io.descoped.dc.api.http;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

import static java.util.Objects.requireNonNull;

public class Headers {

    @JsonProperty("Headers") /* use @JsonWrapper(prefix) in serializer */ Map<String, List<String>> headers = new LinkedHashMap<>();

    public Headers() {
    }

    public Headers(Map<String, List<String>> headersMap) {
        headers.putAll(headersMap);
    }

    public void put(String name, String value) {
        headers.computeIfAbsent(name, values -> new ArrayList<>()).add(value);
    }

    public Optional<String> firstValue(String name) {
        return allValues(name).stream().findFirst();
    }

    public OptionalLong firstValueAsLong(String name) {
        return allValues(name).stream().mapToLong(Long::valueOf).findFirst();
    }

    public List<String> allValues(String name) {
        requireNonNull(name);
        List<String> values = asMap().get(name);
        return values != null ? values : List.of();
    }

    public Map<String, List<String>> asMap() {
        return headers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Headers headers1 = (Headers) o;
        return Objects.equals(headers, headers1.headers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(headers);
    }

    @Override
    public String toString() {
        return "Headers{" +
                "headers=" + headers +
                '}';
    }
}
