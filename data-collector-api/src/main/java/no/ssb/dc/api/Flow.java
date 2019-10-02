package no.ssb.dc.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import no.ssb.dc.api.node.Node;
import no.ssb.dc.api.node.builder.AbstractNodeBuilder;
import no.ssb.dc.api.node.builder.FlowBuilder;
import no.ssb.dc.api.node.builder.NodeBuilderDeserializer;
import no.ssb.dc.api.util.JacksonFactory;

import java.io.IOException;
import java.util.Map;

public class Flow {

    final String name;
    final Node startNode;
    final Map<String, Node> nodeById;

    private Flow(String name, Node startNode, Map<String, Node> nodeById) {
        this.name = name;
        this.startNode = startNode;
        this.nodeById = nodeById;
    }

    public static FlowBuilder start(String name, String startNodeId) {
        return new FlowBuilder(name, startNodeId);
    }

    public static <R extends AbstractNodeBuilder> R deserialize(String source, Class<R> builderClass) {
        try {
            ObjectMapper mapper = JacksonFactory.yamlInstance().objectMapper();
            SimpleModule module = new SimpleModule();
            module.addDeserializer(AbstractNodeBuilder.class, new NodeBuilderDeserializer());
            module.addDeserializer(AbstractNodeBuilder.class, new NodeBuilderDeserializer());
            mapper.registerModule(module);
            return mapper.readValue(source, builderClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String name() {
        return name;
    }

    public Node startNode() {
        return startNode;
    }

    public static Flow create(String name, Node startNode, Map<String, Node> nodeById) {
        return new Flow(name, startNode, nodeById);
    }

}
