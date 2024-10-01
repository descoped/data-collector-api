package io.descoped.dc.api.http;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.X509TrustManager;
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

    Version version();

    Response send(Request request);

    <R> Response send(Request request, BodyHandler<R> bodyHandler);

    CompletableFuture<Response> sendAsync(Request request);

    <R> CompletableFuture<Response> sendAsync(Request request, BodyHandler<R> bodyHandler);

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

        Builder x509TrustManager(X509TrustManager trustManager);

        Builder executor(Executor executor);

        Builder connectTimeout(Duration duration);

        Builder followRedirects(Redirect policy);

        Builder proxy(ProxySelector proxySelector);

        Client build();
    }

}
