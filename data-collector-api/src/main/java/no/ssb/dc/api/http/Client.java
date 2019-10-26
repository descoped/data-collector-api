package no.ssb.dc.api.http;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.net.Authenticator;
import java.net.ProxySelector;
import java.time.Duration;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface Client {

    static Client newClient() {
        return newClientBuilder().build();
    }

    static Builder newClientBuilder() {
        return ServiceLoader.load(Client.Builder.class).findFirst().orElseThrow();
    }

    Response send(Request request);

    CompletableFuture<Response> sendAsync(Request request);

    Object getDelegate();

    enum Version {
        HTTP_1_1,
        HTTP_2
    }

     enum Redirect {

        /**
         * Never redirect.
         */
        NEVER,

        /**
         * Always redirect.
         */
        ALWAYS,

        /**
         * Always redirect, except from HTTPS URLs to HTTP URLs.
         */
        NORMAL
    }

    interface Builder {
        Builder version(Version version);

        Builder priority(int priority);

        Builder authenticator(Authenticator authenticator);

        Builder sslContext(SSLContext sslContext);

        Builder sslParameters(SSLParameters sslParameters);

        Builder executor(Executor executor);

        Builder connectTimeout(Duration duration);

        Builder followRedirects(Redirect policy);

        Builder proxy(ProxySelector proxySelector);


        Client build();
    }

}
