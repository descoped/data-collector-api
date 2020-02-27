package no.ssb.dc.api.node.builder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import no.ssb.dc.api.util.CommonUtils;
import no.ssb.dc.api.util.JsonParser;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

class GenericNodeBuilderDeserializerTest {

    private static final Logger LOG = LoggerFactory.getLogger(GenericNodeBuilderDeserializer.class);

    static <R extends AbstractBuilder> R deserialize(String source, Class<R> builderClass) {
        try {
            ObjectMapper mapper = JsonParser.createYamlParser().mapper();
            SimpleModule module = new SimpleModule();
            module.addDeserializer(GenericNodeBuilderDeserializer.Element.Builder.class, new GenericNodeBuilderDeserializer());
            mapper.registerModule(module);
            return mapper.readValue(source, builderClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Disabled
    @Test
    void testGenericNodeBuilderDeserializer() {
        Path specPath = CommonUtils.currentPath().getParent().resolve("data-collection-consumer-specifications").resolve("specs");
        Path serializedSpec = specPath.resolve("toll-tvinn-test-spec.json");
        String jsonSpec = CommonUtils.readFileOrClasspathResource(serializedSpec.toString());

        AbstractBuilder builder = deserialize(jsonSpec, AbstractBuilder.class);
        LOG.trace("Builder: {}", builder);
        LOG.trace("Spec-path: {}\n{}", serializedSpec.toAbsolutePath(), jsonSpec);
    }
}
