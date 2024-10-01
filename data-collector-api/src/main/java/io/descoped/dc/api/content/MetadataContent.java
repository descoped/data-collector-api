package io.descoped.dc.api.content;

import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import io.descoped.dc.api.CorrelationIds;
import io.descoped.dc.api.http.Headers;
import io.descoped.dc.api.util.JsonParser;

import java.util.Map;
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
        return JsonParser.createJsonParser().toJSON(elementNode);
    }

    public enum ResourceType {
        PAGE,
        ENTRY,
        DOCUMENT
    }

    public static class Builder {

        private ObjectNode metadataNode = JsonParser.createJsonParser().createObjectNode();
        private ObjectNode requestHeaderNode = JsonParser.createJsonParser().createObjectNode();
        private ObjectNode responseHeaderNode = JsonParser.createJsonParser().createObjectNode();
        private ObjectNode httpInfoNode = JsonParser.createJsonParser().createObjectNode();
        private ObjectNode stateNode = JsonParser.createJsonParser().createObjectNode();

        public Builder correlationId(CorrelationIds correlationIds) {
            metadataNode.put("correlation-id", (correlationIds == null ? null : correlationIds.get().stream().map(UUID::toString).collect(Collectors.joining(","))));
            return this;
        }

        public Builder topic(String topic) {
            metadataNode.put("topic", topic);
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

        public Builder statusCode(int statusCode) {
            httpInfoNode.put("statusCode", statusCode);
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

        public Builder state(Map<String, Object> stateMap) {
            if (stateMap == null) {
                return this;
            }
            stateMap.entrySet().forEach(entry -> {
                Object value = entry.getValue();
                ValueNode valueNode;
                if (value instanceof String) {
                    valueNode = new TextNode((String) value);

                } else if (value instanceof Integer) {
                    valueNode = new IntNode((Integer) value);

                } else if (value instanceof Boolean) {
                    valueNode = BooleanNode.valueOf((Boolean) value);

                } else {
                    throw new IllegalStateException("Unsupported state type!");
                }
                stateNode.set(entry.getKey(), valueNode);
            });
            return this;
        }

        public MetadataContent build() {
            JsonParser jsonParser = JsonParser.createJsonParser();
            ObjectNode elementNode = jsonParser.createObjectNode();
            elementNode.set("metadata", metadataNode);
            httpInfoNode.set("request-headers", requestHeaderNode);
            httpInfoNode.set("response-headers", responseHeaderNode);
            elementNode.set("http-info", httpInfoNode);
            if (!stateNode.isEmpty()) {
                elementNode.set("state", stateNode);
            }
            return new MetadataContent(elementNode);
        }
    }
}
