package io.descoped.dc.api.http;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// TODO https://en.wikipedia.org/wiki/List_of_HTTP_status_codes
public enum HttpStatus {

    /* 2XX: generally "OK" */

    HTTP_OK(200, "OK"),
    HTTP_CREATED(201, "Created"),
    HTTP_ACCEPTED(202, "Accepted"),
    HTTP_NOT_AUTHORITATIVE(203, "Non-Authoritative Information"),
    HTTP_NO_CONTENT(204, "No Content"),
    HTTP_RESET(205, "Reset Content"),
    HTTP_PARTIAL(206, "Partial Content"),

    /* 3XX: relocation/redirect */

    HTTP_MULT_CHOICE(300, "Multiple Choices"),
    HTTP_MOVED_PERM(301, "Moved Permanently"),
    HTTP_MOVED_TEMP(302, "Temporary Redirect"),
    HTTP_SEE_OTHER(303, "See Other"),
    HTTP_NOT_MODIFIED(304, "Not Modified"),
    HTTP_USE_PROXY(305, "Use Proxy"),

    /* 4XX: client error */

    HTTP_BAD_REQUEST(400, "Bad Request"),
    HTTP_UNAUTHORIZED(401, "Unauthorized"),
    HTTP_PAYMENT_REQUIRED(402, "Payment Required"),
    HTTP_FORBIDDEN(403, "Forbidden"),
    HTTP_NOT_FOUND(404, "Not Found"),
    HTTP_BAD_METHOD(405, "Method Not Allowed"),
    HTTP_NOT_ACCEPTABLE(406, "Not Acceptable"),
    HTTP_PROXY_AUTH(407, "Proxy Authentication Required"),
    HTTP_CLIENT_TIMEOUT(408, "Request Timeout"),
    HTTP_CONFLICT(409, "Conflict"),
    HTTP_GONE(410, "Gone"),
    HTTP_LENGTH_REQUIRED(411, "Length Required"),
    HTTP_PRECON_FAILED(412, "Precondition Failed"),
    HTTP_ENTITY_TOO_LARGE(413, "Request Entity Too Large"),
    HTTP_REQ_TOO_LONG(414, "Request-URI Too Large"),
    HTTP_UNSUPPORTED_TYPE(415, "Unsupported Media Type"),

    /* 5XX: server error */

    HTTP_INTERNAL_ERROR(500, "Internal Server Error"),
    HTTP_NOT_IMPLEMENTED(501, "Not Implemented"),
    HTTP_BAD_GATEWAY(502, "Bad Gateway"),
    HTTP_UNAVAILABLE(503, "Service Unavailable"),
    HTTP_GATEWAY_TIMEOUT(504, "Gateway Timeout"),
    HTTP_VERSION(505, "HTTP Version Not Supported");

    private static final Map<Integer, HttpStatus> okMap = new LinkedHashMap<>();
    private static final Map<Integer, HttpStatus> redirectionMap = new LinkedHashMap<>();
    private static final Map<Integer, HttpStatus> clientErrorMap = new LinkedHashMap<>();
    private static final Map<Integer, HttpStatus> serverErrorMap = new LinkedHashMap<>();

    private final int statusCode;
    private final String reason;

    HttpStatus(int statusCode, String reason) {
        this.statusCode = statusCode;
        this.reason = reason;
    }

    static Map<Integer, HttpStatus> rangeMap(int fromStatusCodeInclusive, int toStatusCodeInclusive) {
        Map<Integer, HttpStatus> rangeMap = new LinkedHashMap<>();
        for (HttpStatus value : values()) {
            if (value.statusCode >= fromStatusCodeInclusive && value.statusCode <= toStatusCodeInclusive) {
                rangeMap.put(value.statusCode, value);
            }
        }
        return rangeMap;
    }

    static void buildOkMap() {
        if (okMap.isEmpty()) {
            okMap.putAll(rangeMap(200, 299));
        }
    }

    static void buildRedirectionMap() {
        if (redirectionMap.isEmpty()) {
            redirectionMap.putAll(rangeMap(300, 399));
        }
    }

    static void buildClientErrorMap() {
        if (clientErrorMap.isEmpty()) {
            clientErrorMap.putAll(rangeMap(400, 499));
        }
    }

    static void buildServerErrorMap() {
        if (serverErrorMap.isEmpty()) {
            serverErrorMap.putAll(rangeMap(500, 599));
        }
    }

    static public List<HttpStatus> okCodes() {
        buildOkMap();
        return (List<HttpStatus>) okMap.values();
    }

    static public boolean isOk(int statusCode) {
        buildOkMap();
        return okMap.containsKey(statusCode);
    }

    static public List<HttpStatus> redirectionCodes() {
        buildRedirectionMap();
        return (List<HttpStatus>) redirectionMap.values();
    }

    static public boolean isRedirection(int statusCode) {
        buildRedirectionMap();
        return redirectionMap.containsKey(statusCode);
    }

    static public List<HttpStatus> clientErrorList() {
        buildClientErrorMap();
        return (List<HttpStatus>) clientErrorMap.values();
    }

    static public Integer[] clientErrorCodes() {
        return clientErrorList().stream().map(code -> code.statusCode).collect(Collectors.toList()).toArray(new Integer[clientErrorList().size()]);
    }

    static public boolean isClientError(int statusCode) {
        buildClientErrorMap();
        return clientErrorMap.containsKey(statusCode);
    }

    static public List<HttpStatus> serverErrorCodes() {
        buildServerErrorMap();
        return (List<HttpStatus>) serverErrorMap.values();
    }

    static public boolean isServerError(int statusCode) {
        buildServerErrorMap();
        return serverErrorMap.containsKey(statusCode);
    }

    static public List<HttpStatus> range(int fromStatusCodeInclusive, int toStatusCodeInclusive) {
        return new ArrayList<>(rangeMap(fromStatusCodeInclusive, toStatusCodeInclusive).values());
    }

    static public HttpStatus valueOf(int statusCode) {
        for (HttpStatus value : values()) {
            if (value.statusCode == statusCode) {
                return value;
            }
        }
        throw new IllegalStateException("HttpStatusCode is not supported: " + statusCode);
    }

    public int code() {
        return statusCode;
    }

    public String reason() {
        return reason;
    }


}
