package no.ssb.dc.api.http;

import java.util.ServiceLoader;

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
        DELETE
    }

    interface Builder {
        Builder url(String url);

        Builder PUT();

        Builder POST();

        Builder GET();

        Builder DELETE();

        Builder header(String name, String value);

        Request build();
    }

}