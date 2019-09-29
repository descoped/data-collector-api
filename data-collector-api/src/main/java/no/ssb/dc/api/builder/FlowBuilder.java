package no.ssb.dc.api.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import no.ssb.dc.api.Flow;
import no.ssb.dc.api.node.Node;
import no.ssb.dc.api.util.JacksonFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class FlowBuilder extends AbstractNodeBuilder {

    @JsonProperty String flowName;
    @JsonProperty String startNodeId;

    @JsonProperty("nodes") Map<String, NodeBuilder> nodeBuilderById = new LinkedHashMap<>();

    public FlowBuilder(String flowName, String startNodeId) {
        super(BuilderType.Flow);
        this.flowName = flowName;
        this.startNodeId = startNodeId;
    }

    public FlowBuilder node(NodeBuilder builder) {
        nodeBuilderById.put(builder.getId(), builder);
        return this;
    }

    public Flow end() {
        Map<String, Node> nodeInstanceById = new LinkedHashMap<>();

        // add child nodes recursively to nodeInstanceById map
        for (Map.Entry<String, NodeBuilder> entry : nodeBuilderById.entrySet()) {
            nodeInstanceById.put(entry.getKey(), entry.getValue().build(nodeBuilderById, nodeInstanceById));
        }

        return Flow.create(flowName, nodeInstanceById.get(startNodeId), nodeInstanceById);
    }

    public NodeBuilder get(String nodeId) {
        return Optional.ofNullable(nodeBuilderById.get(nodeId)).orElseThrow();
    }

    public String serialize() {
        return JacksonFactory.yamlInstance().toPrettyJSON(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FlowBuilder)) return false;
        FlowBuilder builder = (FlowBuilder) o;
        return Objects.equals(flowName, builder.flowName) &&
                Objects.equals(startNodeId, builder.startNodeId) &&
                Objects.equals(nodeBuilderById, builder.nodeBuilderById);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flowName, startNodeId, nodeBuilderById);
    }

    @Override
    public String toString() {
        return "FlowBuilder{" +
                "flowName='" + flowName + '\'' +
                ", startNodeId='" + startNodeId + '\'' +
                ", nodeBuilderById=" + nodeBuilderById +
                '}';
    }
}
