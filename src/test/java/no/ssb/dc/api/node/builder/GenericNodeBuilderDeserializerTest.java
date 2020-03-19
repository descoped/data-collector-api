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
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static no.ssb.dc.api.node.builder.GenericNodeBuilderDeserializer.Array;
import static no.ssb.dc.api.node.builder.GenericNodeBuilderDeserializer.Field;
import static no.ssb.dc.api.node.builder.GenericNodeBuilderDeserializer.NamedBuilder;
import static no.ssb.dc.api.node.builder.GenericNodeBuilderDeserializer.Node;

class GenericNodeBuilderDeserializerTest {

    private static final Logger LOG = LoggerFactory.getLogger(GenericNodeBuilderDeserializer.class);

    static <R extends Node.NodeBuilder> R deserialize(String source, Class<R> builderClass) {
        try {
            ObjectMapper mapper = JsonParser.createYamlParser().mapper();
            SimpleModule module = new SimpleModule();
            module.addDeserializer(Node.NodeBuilder.class, new GenericNodeBuilderDeserializer());
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

        Node.NodeBuilder builder = deserialize(jsonSpec, Node.NodeBuilder.class);
        traverse(builder);
        LOG.trace("Builder: {}", builder);
        LOG.trace("Spec-path: {}\n{}", serializedSpec.toAbsolutePath(), jsonSpec);
    }

    private void traverse(Node.NodeBuilder builder) {
        GenericNodeBuilderDeserializer.Node rootNode = builder.build();
        LOG.trace("{}", rootNode.name);
    }

    @Disabled
    @Test
    void testModel() {
        Node.NodeBuilder rootBuilder = new Node.NodeBuilder().name("root");

        Field.FieldBuilder child1_FieldBuilder = new Field.FieldBuilder().name("field1");
        rootBuilder.addChild(child1_FieldBuilder);

        Node.NodeBuilder child1_NodeBuilder = new Node.NodeBuilder().name("child1");
        rootBuilder.addChild(child1_NodeBuilder);

        Array.ArrayBuilder child1_ArrayBuilder = new Array.ArrayBuilder();
        rootBuilder.addChild("array1", child1_ArrayBuilder);

        traverse(0, new ArrayList<>(), rootBuilder, (ancestors, current) -> {

        });
    }

    void traverse(int depth, List<NamedBuilder> ancestors,
                  NamedBuilder currentBuilder,
                  BiConsumer<List<NamedBuilder>, NamedBuilder> visit) {

        visit.accept(ancestors, currentBuilder);

        for (Object childBuilder : currentBuilder.getChildren().values()) {

        }
    }

}
