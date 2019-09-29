package no.ssb.dc.api.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class JacksonFactory {

    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static final ObjectMapper YAML_OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());

    private final ObjectMapper objectMapper;

    public JacksonFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
//        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
    }

    public ObjectNode createObjectNode() {
        return objectMapper.createObjectNode();
    }

    public ArrayNode createArrayNode() {
        return objectMapper.createArrayNode();
    }

    public <T> T fromJson(InputStream source, Class<T> clazz) {
        try {
            String json = new String(source.readAllBytes(), StandardCharsets.UTF_8);
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T fromJson(String source, Class<T> clazz) {
        try {
            return objectMapper.readValue(source, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String toJSON(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String toPrettyJSON(Object value) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static JacksonFactory instance() {
        return new JacksonFactory(OBJECT_MAPPER);
    }

    public static JacksonFactory yamlInstance() {
        return new JacksonFactory(YAML_OBJECT_MAPPER);
    }

    public ObjectMapper objectMapper() {
        return objectMapper;
    }

}
