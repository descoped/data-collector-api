package io.descoped.dc.api.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

class YamlParserImpl extends JsonParserImpl {

    static final ObjectMapper YAML_OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());

    public YamlParserImpl() {
        super(YAML_OBJECT_MAPPER);
    }

    public YamlParserImpl(YAMLFactory yamlFactory) {
        super(new ObjectMapper(yamlFactory));
    }

    public YamlParserImpl(ObjectMapper objectMapper) {
        super(objectMapper);
    }

}
