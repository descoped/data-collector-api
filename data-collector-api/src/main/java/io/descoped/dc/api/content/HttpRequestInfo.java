package io.descoped.dc.api.content;

import io.descoped.dc.api.CorrelationIds;
import io.descoped.dc.api.http.Headers;

public class HttpRequestInfo {

    private final CorrelationIds correlationIds;
    private final String url;
    private final Headers requestHeaders;
    private final Headers responseHeaders;
    private final long requestDurationNanoSeconds;
    private int statusCode;

    public HttpRequestInfo(CorrelationIds correlationIds, String url, int statusCode, Headers requestHeaders, Headers responseHeaders, long requestDurationNanoSeconds) {
        this.correlationIds = correlationIds;
        this.url = url;
        this.statusCode = statusCode;
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

    public int getStatusCode() {
        return statusCode;
    }

}
