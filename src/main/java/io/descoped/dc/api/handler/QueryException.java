package io.descoped.dc.api.handler;

import io.descoped.dc.api.error.ExecutionException;

public class QueryException extends ExecutionException {
    public QueryException() {
        super();
    }

    public QueryException(String message) {
        super(message);
    }

    public QueryException(String message, Throwable cause) {
        super(message, cause);
    }

    public QueryException(Throwable cause) {
        super(cause);
    }
}
