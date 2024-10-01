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
     * Use: Response.<TYPE>bodyHandler()
     *
     * @param <R>
     * @return
     */
    <R> Optional<BodyHandler<R>> bodyHandler();

    Optional<Response> previousResponse();

    interface Builder {
        Builder delegate(Object delegate);

        <R> void bodyHandler(BodyHandler<R> bodyHandler);

        Response build();
    }

}
