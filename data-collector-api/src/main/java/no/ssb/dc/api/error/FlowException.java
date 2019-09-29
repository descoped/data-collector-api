package no.ssb.dc.api.error;

public class FlowException extends RuntimeException {

    public FlowException() {
        super();
    }

    public FlowException(String message) {
        super(message);
    }

    public FlowException(String message, Throwable cause) {
        super(message, cause);
    }

    public FlowException(Throwable cause) {
        super(cause);
    }

    protected FlowException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
