package no.ssb.dc.api.content;

import com.fasterxml.jackson.databind.node.ObjectNode;
import no.ssb.dc.api.CorrelationIds;
import no.ssb.dc.api.http.Headers;
import no.ssb.dc.api.util.JacksonFactory;

import java.util.UUID;
import java.util.stream.Collectors;

public class MetadataContent {

    private final ObjectNode elementNode;

    public MetadataContent(ObjectNode elementNode) {
        this.elementNode = elementNode;
    }

    public ObjectNode getElementNode() {
        return elementNode;
    }

    public String toJSON() {
        return JacksonFactory.instance().toJSON(elementNode);
    }

    public enum ResourceType {
        PAGE,
        ENTRY,
        DOCUMENT
    }

    public static class Builder {

        private ObjectNode metadataNode = JacksonFactory.instance().createObjectNode();
        private ObjectNode requestHeaderNode = JacksonFactory.instance().createObjectNode();
        private ObjectNode responseHeaderNode = JacksonFactory.instance().createObjectNode();

        public Builder correlationId(CorrelationIds correlationIds) {
            metadataNode.put("correlation-id", (correlationIds == null ? null : correlationIds.get().stream().map(UUID::toString).collect(Collectors.joining(","))));
            return this;
        }

        public Builder namespace(String namespace) {
            metadataNode.put("namespace", namespace);
            return this;
        }

        public Builder position(String position) {
            metadataNode.put("position", position);
            return this;
        }

        public Builder contentKey(String contentKey) {
            metadataNode.put("content-key", contentKey);
            return this;
        }

        public Builder resourceType(ResourceType resourceType) {
            metadataNode.put("resource-type", resourceType.name().toLowerCase());
            return this;
        }

        public Builder contentType(String contentType) {
            metadataNode.put("content-type", contentType);
            return this;
        }

        public Builder contentLength(int contentLength) {
            metadataNode.put("content-length", contentLength);
            return this;
        }

        public Builder requestDurationNanoTime(long requestDurationNanoTime) {
            metadataNode.put("request-duration-nano-time", requestDurationNanoTime);
            return this;
        }

        public Builder url(String url) {
            metadataNode.put("url", url);
            return this;
        }

        public Builder requestHeaders(Headers requestHeaders) {
            requestHeaders.asMap().forEach((key, value) -> requestHeaderNode.put(key, String.join(",", value)));
            return this;
        }

        public Builder responseHeaders(Headers responseHeaders) {
            responseHeaders.asMap().forEach((key, value) -> responseHeaderNode.put(key, String.join(",", value)));
            return this;
        }

        public MetadataContent build() {
            ObjectNode elementNode = JacksonFactory.instance().createObjectNode();
            elementNode.set("metadata", metadataNode);
            ObjectNode httpInfoNode = JacksonFactory.instance().createObjectNode();
            httpInfoNode.set("request-headers", requestHeaderNode);
            httpInfoNode.set("response-headers", responseHeaderNode);
            elementNode.set("http-info", httpInfoNode);
            return new MetadataContent(elementNode);
        }
    }
}
