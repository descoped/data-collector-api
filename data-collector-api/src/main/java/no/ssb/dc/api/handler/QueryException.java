package no.ssb.dc.api.handler;

import no.ssb.dc.api.error.ExecutionException;

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
