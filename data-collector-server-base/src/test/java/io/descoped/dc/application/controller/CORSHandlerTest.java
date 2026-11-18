package io.descoped.dc.application.controller;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class CORSHandlerTest {

    volatile private HttpServerExchange httpServerExchange;
    volatile private HttpHandler customHandler;
    private HttpClient client;
    private Undertow server;
    private URI uri;

    /**
     * Find a free port for the undertow server.
     */
    static int findFree() {
        int port = 0;
        while (port == 0) {
            ServerSocket socket = null;
            try {
                socket = new ServerSocket(0);
                port = socket.getLocalPort();
            } catch (IOException e) {
                throw new RuntimeException("Failed finding free port", e);
            } finally {
                try {
                    if (socket != null) socket.close();
                } catch (IOException ignore) {
                }
            }
        }
        return port;
    }

    @AfterEach
    public void tearDown() {
        server.stop();
    }

    @BeforeEach
    public void setUp() {

        httpServerExchange = null;
        customHandler = null;
        HttpHandler httpHandler = exchange -> {
            exchange.setStatusCode(222);
            if (customHandler != null) {
                customHandler.handleRequest(exchange);
            }
            this.httpServerExchange = exchange;
        };

        List<Pattern> patterns = List.of(
                Pattern.compile("http://foo"),
                Pattern.compile("https://bar"),
                Pattern.compile("https?://(regexp|pxeger)"));
        CORSHandler corsHandler = new CORSHandler(httpHandler, patterns, true, 444,
                123456, Set.of("GET", "PUT"), Set.of("x-foo", "y-bar"));

        int port = findFree();
        server = Undertow.builder().addHttpListener(port, "localhost").setHandler(corsHandler).build();
        client = HttpClient.newHttpClient();
        server.start();

        uri = URI.create("http://localhost:" + port);
    }

    private HttpResponse<Void> send(String method, String origin, String... headers) throws IOException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                .method(method, HttpRequest.BodyPublishers.noBody())
                .header("Origin", origin);
        if (headers.length > 0) {
            builder.headers(headers);
        }
        try {
            return client.send(builder.build(), HttpResponse.BodyHandlers.discarding());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e);
        }
    }

    private static String header(HttpResponse<?> response, Object name) {
        return response.headers().firstValue(name.toString()).orElse(null);
    }

    @Test
    public void testThatExchangeIsForwarded() throws IOException {
        HttpResponse<Void> response = send("GET", "http://foo");
        assertThat(response.statusCode()).isEqualTo(222);
        assertThat(header(response, CORSHandler.ACCESS_CONTROL_ALLOW_ORIGIN)).isEqualTo("http://foo");
        assertThat(header(response, CORSHandler.ACCESS_CONTROL_EXPOSE_HEADERS))
                .contains("y-bar").contains("x-foo").contains(",");
        assertThat(httpServerExchange).isNotNull();
    }

    @Test
    public void testThatPreflightShortCircuitsHandler() throws IOException {
        HttpResponse<Void> response = send("OPTIONS", "http://foo", "Access-Control-Request-Method", "PUT");
        assertThat(response.statusCode()).isEqualTo(444);
        assertThat(header(response, CORSHandler.ACCESS_CONTROL_ALLOW_METHODS))
                .contains("PUT").contains("GET").contains(",");
        assertThat(header(response, CORSHandler.ACCESS_CONTROL_ALLOW_ORIGIN)).isEqualTo("http://foo");
        assertThat(header(response, CORSHandler.ACCESS_CONTROL_ALLOW_HEADERS))
                .contains("y-bar").contains("x-foo").contains(",");
        assertThat(header(response, CORSHandler.ACCESS_CONTROL_ALLOW_CREDENTIALS)).isEqualTo("true");
        assertThat(header(response, CORSHandler.ACCESS_CONTROL_MAX_AGE)).isEqualTo("123456");
        assertThat(httpServerExchange).isNull();
    }

    @Test
    public void testThatItIncludesVary() throws IOException {
        HttpResponse<Void> response = send("GET", "http://foo");
        assertThat(header(response, Headers.VARY_STRING)).isEqualTo("Origin");
        assertThat(httpServerExchange).isNotNull();
    }

    @Test
    public void testThatItAppendsVary() throws IOException {
        customHandler = exchange -> exchange.getResponseHeaders().add(Headers.VARY, "Foo");
        HttpResponse<Void> response = send("GET", "http://foo");
        assertThat(response.headers().allValues(Headers.VARY_STRING)).containsExactlyInAnyOrder("Origin", "Foo");
        assertThat(httpServerExchange).isNotNull();
    }

    @Test
    public void testMatchesRegex() throws IOException {
        HttpResponse<Void> response1 = send("GET", "http://regexp");
        assertThat(header(response1, CORSHandler.ACCESS_CONTROL_ALLOW_ORIGIN)).isEqualTo("http://regexp");
        assertThat(httpServerExchange).isNotNull();

        HttpResponse<Void> response2 = send("GET", "https://pxeger");
        assertThat(header(response2, CORSHandler.ACCESS_CONTROL_ALLOW_ORIGIN)).isEqualTo("https://pxeger");
        assertThat(httpServerExchange).isNotNull();
    }

}
