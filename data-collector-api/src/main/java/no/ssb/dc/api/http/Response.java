package no.ssb.dc.api.http;

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

    Optional<Response> previousResponse();

    interface Builder {
        Builder delegate(Object delegate);

        Response build();
    }

}
