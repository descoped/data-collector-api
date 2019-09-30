package no.ssb.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import no.ssb.dc.api.node.Base;
import no.ssb.dc.api.node.Node;
import no.ssb.dc.api.node.Parallel;
import no.ssb.dc.api.node.Publish;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
    <R extends Base> R build(Map<String, NodeBuilder> nodeBuilderById, Map<String, R> nodeInstanceById) {
        QueryBuilder.QueryNode splitToListQueryNode = (QueryBuilder.QueryNode) splitBuilder.build(nodeBuilderById, nodeInstanceById);

        Map<String, QueryBuilder.QueryNode> contextVariablesMap = new LinkedHashMap<>();
        for (Map.Entry<String, QueryBuilder> entry : variables.entrySet()) {
            QueryBuilder.QueryNode node = (QueryBuilder.QueryNode) entry.getValue().build(nodeBuilderById, nodeInstanceById);
            contextVariablesMap.put(entry.getKey(), node);
        }

        List<Node> stepList = new ArrayList<>();
        for (NodeBuilder builder : steps) {
            Node node = (Node) builder.build(nodeBuilderById, nodeInstanceById);
            stepList.add(node);
        }

        PublishBuilder.PublishNode publishNode = publishBuilder == null ? null : (PublishBuilder.PublishNode) publishBuilder.build(nodeBuilderById, nodeInstanceById);

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

    static class ParallelNode extends FlowNode implements Parallel {

        final QueryBuilder.QueryNode splitQueryNode;
        final Map<String, QueryBuilder.QueryNode> variables;
        final List<Node> steps;
        final Publish publishNode;

        ParallelNode(QueryBuilder.QueryNode splitQueryNode, Map<String, QueryBuilder.QueryNode> variables, List<Node> steps, Publish publishNode) {
            this.splitQueryNode = splitQueryNode;
            this.variables = variables;
            this.steps = steps;
            this.publishNode = publishNode;
        }

        @Override
        public QueryBuilder.QueryNode splitQuery() {
            return splitQueryNode;
        }

        @Override
        public Set<String> variableNames() {
            return variables.keySet();
        }

        @Override
        public QueryBuilder.QueryNode variable(String name) {
            return variables.get(name);
        }

        @Override
        public List<Node> steps() {
            return steps;
        }

        @Override
        public Publish publish() {
            return publishNode;
        }

        @Override
        public Iterator<? extends Node> iterator() {
            return steps.iterator();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ParallelNode that = (ParallelNode) o;
            return Objects.equals(splitQueryNode, that.splitQueryNode) &&
                    Objects.equals(variables, that.variables) &&
                    Objects.equals(steps, that.steps) &&
                    Objects.equals(publishNode, that.publishNode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(splitQueryNode, variables, steps, publishNode);
        }

        @Override
        public String toString() {
            return "ParallelNode{" +
                    "splitBuilder=" + splitQueryNode +
                    ", variables=" + variables +
                    ", steps=" + steps +
                    ", publishBuilder=" + publishNode +
                    '}';
        }
    }
}
