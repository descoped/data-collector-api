package no.ssb.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import no.ssb.dc.api.Flow;
import no.ssb.dc.api.node.Configurations;
import no.ssb.dc.api.node.Node;
import no.ssb.dc.api.util.JacksonFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class FlowBuilder extends AbstractBuilder {

    @JsonProperty String flowName;
    @JsonProperty String startNodeId;

    @JsonProperty("configure") List<ConfigurationBuilder> configurationBuilders = new ArrayList<>();
    @JsonProperty("nodes") Map<String, NodeBuilder> nodeBuilderById = new LinkedHashMap<>();

    public FlowBuilder(String flowName, String startNodeId) {
        super(BuilderType.Flow);
        this.flowName = flowName;
        this.startNodeId = startNodeId;
    }

    public FlowBuilder configure(ConfigurationBuilder builder) {
        configurationBuilders.add(builder);
        return this;
    }

    public FlowBuilder node(NodeWithIdBuilder builder) {
        nodeBuilderById.put(builder.getId(), builder);
        return this;
    }

    public Flow end() {
        BuildContext buildContext = BuildContext.fromNodeBuilderById(nodeBuilderById);

        Configurations.Builder configurationsBuilder = new Configurations.Builder();
        for (ConfigurationBuilder configurationBuilder : configurationBuilders) {
            configurationsBuilder.add(configurationBuilder.build(buildContext));
        }
        Configurations configurations = configurationsBuilder.build();

        // add child nodes recursively to buildContext.nodeInstanceById map
        for (Map.Entry<String, NodeBuilder> entry : nodeBuilderById.entrySet()) {
            String nodeBuilderId = entry.getKey();
            NodeBuilder nodeBuilder = entry.getValue();
            Node nodeInstance = nodeBuilder.build(buildContext);
            buildContext.cacheInstance(nodeBuilderId, nodeInstance);
        }

        return Flow.create(flowName, configurations, buildContext.getInstance(startNodeId), buildContext.nodeInstanceById());
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
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        FlowBuilder that = (FlowBuilder) o;
        return flowName.equals(that.flowName) &&
                startNodeId.equals(that.startNodeId) &&
                Objects.equals(nodeBuilderById, that.nodeBuilderById);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), flowName, startNodeId, nodeBuilderById);
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
