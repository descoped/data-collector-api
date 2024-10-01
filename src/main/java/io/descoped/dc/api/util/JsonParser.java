package io.descoped.dc.api.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.InputStream;

public interface JsonParser {

    ObjectMapper mapper();

    ObjectNode createObjectNode();

    ArrayNode createArrayNode();

    <T> T fromJson(InputStream source, Class<T> clazz);

    <T> T fromJson(String source, Class<T> clazz);

    String toJSON(Object value);

    String toPrettyJSON(Object value);

    static JsonParser createJsonParser() {
        return new JsonParserImpl();
    }

    static JsonParser createJsonParser(ObjectMapper objectMapper) {
        return new JsonParserImpl(objectMapper);
    }

    static JsonParser createYamlParser() {
        return new YamlParserImpl();
    }

    static JsonParser createYamlParser(YAMLFactory yamlFactory) {
        return new YamlParserImpl(yamlFactory);
    }

    static JsonParser createYamlParser(ObjectMapper objectMapper) {
        return new YamlParserImpl(objectMapper);
    }

}
