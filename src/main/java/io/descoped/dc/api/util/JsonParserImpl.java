package io.descoped.dc.api.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

class JsonParserImpl implements JsonParser {

    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final ObjectMapper mapper;

    public JsonParserImpl() {
        mapper = OBJECT_MAPPER;
    }

    public JsonParserImpl(ObjectMapper objectMapper) {
        mapper = objectMapper;
    }

    @Override
    public ObjectMapper mapper() {
        return mapper;
    }

    @Override
    public ObjectNode createObjectNode() {
        return mapper.createObjectNode();
    }

    @Override
    public ArrayNode createArrayNode() {
        return mapper.createArrayNode();
    }

    @Override
    public <T> T fromJson(InputStream source, Class<T> clazz) {
        try {
            String json = new String(source.readAllBytes(), StandardCharsets.UTF_8);
            return mapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T fromJson(String source, Class<T> clazz) {
        try {
            return mapper.readValue(source, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toJSON(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toPrettyJSON(Object value) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
