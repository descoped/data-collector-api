package no.ssb.dc.api.http;

import no.ssb.dc.api.CorrelationIds;

public class Metadata {

    private final CorrelationIds correlationIds;
    private final String url;
    private final Headers requestHeaders;
    private final Headers responseHeaders;
    private final long requestDurationNanoSeconds;

    public Metadata(CorrelationIds correlationIds, String url, Headers requestHeaders, Headers responseHeaders, long requestDurationNanoSeconds) {
        this.correlationIds = correlationIds;
        this.url = url;
        this.requestHeaders = requestHeaders;
        this.responseHeaders = responseHeaders;
        this.requestDurationNanoSeconds = requestDurationNanoSeconds;
    }

    public CorrelationIds getCorrelationIds() {
        return correlationIds;
    }

    public String getUrl() {
        return url;
    }

    public Headers getRequestHeaders() {
        return requestHeaders;
    }

    public Headers getResponseHeaders() {
        return responseHeaders;
    }

    public long getRequestDurationNanoSeconds() {
        return requestDurationNanoSeconds;
    }
}
