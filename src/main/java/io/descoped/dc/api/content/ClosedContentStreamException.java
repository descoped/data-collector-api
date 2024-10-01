package io.descoped.dc.api.content;

public class ClosedContentStreamException extends RuntimeException {
    public ClosedContentStreamException() {
        super();
    }

    public ClosedContentStreamException(String message) {
        super(message);
    }

    public ClosedContentStreamException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClosedContentStreamException(Throwable cause) {
        super(cause);
    }

    protected ClosedContentStreamException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
