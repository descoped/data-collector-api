package io.descoped.dc.api.handler;

import java.util.Objects;

public class QueryState<T> {

    private final QueryFeature.Type type;
    private final T data;

    public QueryState(QueryFeature.Type type, T data) {
        this.type = type;
        this.data = data;
    }

    public QueryFeature.Type type() {
        return type;
    }

    public T data() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueryState<?> that = (QueryState<?>) o;
        return type == that.type &&
                data.equals(that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, data);
    }

    @Override
    public String toString() {
        return "QueryState{" +
                "type=" + type +
                ", data=" + data +
                '}';
    }
}
