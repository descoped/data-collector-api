package no.ssb.dc.api.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import no.ssb.dc.api.NodeBuilderDeserializer;
import no.ssb.dc.api.node.BaseNode;
import no.ssb.dc.api.node.Node;
import no.ssb.dc.api.node.impl.ParallelNode;
import no.ssb.dc.api.node.impl.PublishNode;
import no.ssb.dc.api.node.impl.QueryNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class ParallelBuilder extends NodeBuilder {

    @JsonProperty("splitQuery") QueryBuilder splitBuilder;
    @JsonProperty Map<String, QueryBuilder> variables = new LinkedHashMap<>();
    @JsonProperty List<NodeBuilder> steps = new ArrayList<>();
    @JsonProperty("publish") PublishBuilder publishBuilder;

    public ParallelBuilder(QueryBuilder splitBuilder) {
        super(BuilderType.Parallel);
        this.splitBuilder = splitBuilder;
    }

    public ParallelBuilder variable(String identifier, QueryBuilder queryBuilder) {
        variables.put(identifier, queryBuilder);
        return this;
    }

    public ParallelBuilder step(NodeBuilder builder) {
        steps.add(builder);
        return this;
    }

    public ParallelBuilder publish(PublishBuilder publishBuilder) {
        this.publishBuilder = publishBuilder;
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    <R extends BaseNode> R build(Map<String, NodeBuilder> nodeBuilderById, Map<String, R> nodeInstanceById) {
        QueryNode splitToListQueryNode = (QueryNode) splitBuilder.build(nodeBuilderById, nodeInstanceById);

        Map<String, QueryNode> contextVariablesMap = new LinkedHashMap<>();
        for (Map.Entry<String, QueryBuilder> entry : variables.entrySet()) {
            QueryNode node = (QueryNode) entry.getValue().build(nodeBuilderById, nodeInstanceById);
            contextVariablesMap.put(entry.getKey(), node);
        }

        List<Node> stepList = new ArrayList<>();
        for (NodeBuilder builder : steps) {
            Node node = (Node) builder.build(nodeBuilderById, nodeInstanceById);
            stepList.add(node);
        }

        PublishNode publishNode = publishBuilder == null ? null : (PublishNode) publishBuilder.build(nodeBuilderById, nodeInstanceById);

        return (R) new ParallelNode(splitToListQueryNode, contextVariablesMap, stepList, publishNode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParallelBuilder)) return false;
        ParallelBuilder that = (ParallelBuilder) o;
        return Objects.equals(splitBuilder, that.splitBuilder) &&
                Objects.equals(variables, that.variables) &&
                Objects.equals(steps, that.steps) &&
                Objects.equals(publishBuilder, that.publishBuilder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(splitBuilder, variables, steps, publishBuilder);
    }

    @Override
    public String toString() {
        return "ParallelBuilder{" +
                "splitBuilder=" + splitBuilder +
                ", variables=" + variables +
                ", steps=" + steps +
                ", publishBuilder=" + publishBuilder +
                '}';
    }
}
