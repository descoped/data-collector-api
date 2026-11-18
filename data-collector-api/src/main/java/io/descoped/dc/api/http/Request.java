package io.descoped.dc.api.http;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ServiceLoader;
import java.util.concurrent.Flow;

public interface Request {

    static Builder newRequestBuilder() {
        return ServiceLoader.load(Request.Builder.class).findFirst().orElseThrow();
    }

    String url();

    Method method();

    Headers headers();

    Object getDelegate();

    enum Method {
        PUT,
        POST,
        GET,
        DELETE,
        OPTIONS;
    }

    interface Builder {
        Builder url(String url);

        Builder PUT(byte[] bytes);

        Builder PUT(Flow.Publisher<ByteBuffer> bodyPublisher);

        Builder POST(byte[] bytes);

        Builder POST(Flow.Publisher<ByteBuffer> bodyPublisher);

        Builder GET();

        Builder DELETE();

        Builder header(String name, String value);

        Builder expectContinue(boolean enable);

        Builder timeout(Duration duration);

        Request build();
    }

}
