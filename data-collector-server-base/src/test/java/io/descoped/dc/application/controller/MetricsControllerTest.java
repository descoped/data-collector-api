package io.descoped.dc.application.controller;

import io.descoped.config.DynamicConfiguration;
import io.descoped.config.StoreBasedDynamicConfiguration;
import io.descoped.dc.application.server.UndertowApplication;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.Summary;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Instant;
import java.util.Optional;

import static io.descoped.dc.api.util.CommonUtils.captureStackTrace;
import static io.descoped.dc.application.controller.CORSHandlerTest.findFree;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MetricsControllerTest {

    private static final Logger LOG = LoggerFactory.getLogger(MetricsControllerTest.class);

    private static final HttpClient client = HttpClient.newHttpClient();
    private static UndertowApplication server;

    @BeforeAll
    public static void beforeAll() {
        DynamicConfiguration configuration = new StoreBasedDynamicConfiguration.Builder()
                .values("prometheus.defaultExports.disabled", "true")
                .build();
        server = UndertowApplication.initializeUndertowApplication(configuration, findFree(), null);
        server.start();
    }

    @AfterAll
    public static void afterAll() {
        server.stop();
    }

    static public HttpResponse GET(String uri, String... headers) {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(
                    URI.create("http://" + server.getHost() + ":" + server.getPort() + uri)).GET();
            if (headers != null && headers.length > 1) {
                requestBuilder.headers(headers);
            }
            java.net.http.HttpResponse<String> response = client.send(requestBuilder.build(),
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            return new HttpResponse(response.statusCode(), response.body());
        } catch (Exception e) {
            LOG.error("Error: {}", captureStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    @Test
    public void thatMetricsAreHealth() {
        HttpResponse response = GET("/metrics/-/healthy");
        assertEquals(200, response.statusCode);
        assertEquals("Exporter is Healthy.", response.body);
    }

    @Test
    public void thatMetricsAreReported() throws IOException, InterruptedException {
        HttpResponse response = GET("/metrics");
        assertEquals(200, response.statusCode);
        LOG.trace("body:\n{}", response.body);
    }

    @Test
    public void experiment() throws InterruptedException {
        Counter requests = Counter.build("requests_total", "Total requests").namespace("ns").subsystem("foo").register();
        requests.inc();

        Counter request_count = Counter.build("requests_count", "Count requests").namespace("ns").subsystem("foo").labelNames("url").register();
        requests.inc();

        Gauge gauge = Gauge.build("currentTime", "Current Time").namespace("ns").subsystem("foo").register();
        gauge.set(Instant.now().toEpochMilli());

        {
            // null value causes NPE
            Optional<String> emptyString = Optional.empty();
            request_count.labels(emptyString.orElse("")).inc();
        }

        {
            Histogram histogram = Histogram.build("histogram_KEY", "Histogram").register();
            {
                Histogram.Timer timer = histogram.startTimer();
                Thread.sleep(250);
                histogram.observe(1500);
                timer.observeDuration();
            }
            Thread.sleep(500);

            {
                Histogram.Timer timer = histogram.startTimer();
                Thread.sleep(220);
                histogram.observe(1200);
                timer.observeDuration();
            }
        }

        {
            Summary summary = Summary.build("someSummary", "Some Summary").register();
            Summary.Timer timer = summary.startTimer();
            Thread.sleep(250);
            summary.observe(1500);
            timer.observeDuration();
        }


    }

    static class HttpResponse {
        public final int statusCode;
        public final String body;

        public HttpResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }
    }
}
