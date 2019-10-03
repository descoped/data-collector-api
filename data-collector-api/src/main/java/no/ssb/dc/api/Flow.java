package no.ssb.dc.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import no.ssb.dc.api.node.Configurations;
import no.ssb.dc.api.node.Node;
import no.ssb.dc.api.node.builder.AbstractBuilder;
import no.ssb.dc.api.node.builder.FlowBuilder;
import no.ssb.dc.api.node.builder.NodeBuilderDeserializer;
import no.ssb.dc.api.util.JacksonFactory;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class Flow {

    final String name;
    final Configurations configurations;
    final Node startNode;
    final Map<String, Node> nodeById;

    private Flow(String name, Node startNode, Map<String, Node> nodeById) {
        this(name, new Configurations(new LinkedHashMap<>()), startNode, nodeById);
    }

    private Flow(String name, Configurations configurations, Node startNode, Map<String, Node> nodeById) {
        this.name = name;
        this.configurations = configurations;
        this.startNode = startNode;
        this.nodeById = nodeById;
    }

    public static FlowBuilder start(String name, String startNodeId) {
        return new FlowBuilder(name, startNodeId);
    }

    public static <R extends AbstractBuilder> R deserialize(String source, Class<R> builderClass) {
        try {
            ObjectMapper mapper = JacksonFactory.yamlInstance().objectMapper();
            SimpleModule module = new SimpleModule();
            module.addDeserializer(AbstractBuilder.class, new NodeBuilderDeserializer());
            mapper.registerModule(module);
            return mapper.readValue(source, builderClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Flow create(String name, Node startNode, Map<String, Node> nodeById) {
        return new Flow(name, startNode, nodeById);
    }

    public static Flow create(String name, Configurations configurations, Node startNode, Map<String, Node> nodeById) {
        return new Flow(name, configurations, startNode, nodeById);
    }

    public String name() {
        return name;
    }

    public Node startNode() {
        return startNode;
    }

}
