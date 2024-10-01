package io.descoped.dc.api.handler;

import java.util.Objects;

public class QueryResult<T> {

    private final T result;

    public QueryResult(T result) {
        this.result = result;
    }

    public T getResult() {
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueryResult<?> that = (QueryResult<?>) o;
        return Objects.equals(result, that.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(result);
    }

    @Override
    public String toString() {
        return "QueryResult{" +
                "result=" + result +
                '}';
    }
}
