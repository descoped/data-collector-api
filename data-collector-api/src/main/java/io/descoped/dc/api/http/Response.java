package io.descoped.dc.api.http;

import java.util.Optional;
import java.util.ServiceLoader;

public interface Response {

    static Builder newResponseBuilder() {
        return ServiceLoader.load(Response.Builder.class).findFirst().orElseThrow();
    }

    String url();

    Headers headers();

    int statusCode();

    byte[] body();

    /**
     * The body handler that received this response's body, typed by the caller: {@code response.<TYPE>bodyHandler()}.
     *
     * @param <R> the body type the handler produces
     * @return the body handler, or empty when the body was read into memory
     */
    <R> Optional<BodyHandler<R>> bodyHandler();

    Optional<Response> previousResponse();

    interface Builder {
        Builder delegate(Object delegate);

        <R> void bodyHandler(BodyHandler<R> bodyHandler);

        Response build();
    }

}
