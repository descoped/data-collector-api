package io.descoped.dc.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.descoped.dc.api.node.Configurations;
import io.descoped.dc.api.node.Node;
import io.descoped.dc.api.node.builder.AbstractBuilder;
import io.descoped.dc.api.node.builder.BuildContext;
import io.descoped.dc.api.node.builder.NodeBuilderDeserializer;
import io.descoped.dc.api.node.builder.SpecificationBuilder;
import io.descoped.dc.api.node.builder.SpecificationContextBuilder;
import io.descoped.dc.api.util.JsonParser;

import java.io.IOException;
import java.util.Map;

public class Specification {

    final String specificationId;
    final String name;
    final Configurations configurations;
    final Node startFunction;
    final Map<String, Node> nodeById;

    private Specification(String specificationId, String name, Node startFunction, Map<String, Node> nodeById) {
        this(specificationId, name, new Configurations.Builder().add(new SpecificationContextBuilder().build(BuildContext.empty())).build(), startFunction, nodeById);
    }

    private Specification(String specificationId, String name, Configurations configurations, Node startFunction, Map<String, Node> nodeById) {
        this.specificationId = specificationId;
        this.name = name;
        this.configurations = configurations;
        this.startFunction = startFunction;
        this.nodeById = nodeById;
    }

    public static SpecificationBuilder start(String specificationId, String name, String startFunction) {
        return new SpecificationBuilder(specificationId, name, startFunction);
    }

    public static SpecificationBuilder deserialize(String source) {
        return deserialize(source, SpecificationBuilder.class);
    }

    public static <R extends AbstractBuilder> R deserialize(String source, Class<R> builderClass) {
        try {
            ObjectMapper mapper = JsonParser.createYamlParser().mapper();
            SimpleModule module = new SimpleModule();
            module.addDeserializer(AbstractBuilder.class, new NodeBuilderDeserializer());
            mapper.registerModule(module);
            return mapper.readValue(source, builderClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Specification create(String specificationId, String name, Node startFunction, Map<String, Node> nodeById) {
        return new Specification(specificationId, name, startFunction, nodeById);
    }

    public static Specification create(String specificationId, String name, Configurations configurations, Node startFunction, Map<String, Node> nodeById) {
        return new Specification(specificationId, name, configurations, startFunction, nodeById);
    }

    public String id() {
        return specificationId;
    }

    public String name() {
        return name;
    }

    public Node startFunction() {
        return startFunction;
    }

}
