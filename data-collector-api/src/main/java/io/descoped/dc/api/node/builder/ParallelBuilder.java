package io.descoped.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.descoped.dc.api.node.Base;
import io.descoped.dc.api.node.Configurations;
import io.descoped.dc.api.node.Node;
import io.descoped.dc.api.node.Parallel;
import io.descoped.dc.api.node.Publish;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class ParallelBuilder extends NodeBuilder {

    @JsonProperty("splitQuery")
    QueryBuilder splitBuilder;
    @JsonProperty
    Map<String, QueryBuilder> variables = new LinkedHashMap<>();
    @JsonProperty("pipes")
    List<NodeBuilder> pipes = new ArrayList<>();
    @JsonProperty("publish")
    PublishBuilder publishBuilder;

    public ParallelBuilder(QueryBuilder splitBuilder) {
        super(BuilderType.Parallel);
        this.splitBuilder = splitBuilder;
    }

    public ParallelBuilder variable(String identifier, QueryBuilder queryBuilder) {
        variables.put(identifier, queryBuilder);
        return this;
    }

    public ParallelBuilder pipe(NodeBuilder builder) {
        pipes.add(builder);
        return this;
    }

    public ParallelBuilder publish(PublishBuilder publishBuilder) {
        this.publishBuilder = publishBuilder;
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    <R extends Base> R build(BuildContext buildContext) {
        QueryBuilder.QueryNode splitToListQueryNode = splitBuilder.build(buildContext);

        Map<String, QueryBuilder.QueryNode> contextVariablesMap = new LinkedHashMap<>();
        for (Map.Entry<String, QueryBuilder> entry : variables.entrySet()) {
            QueryBuilder.QueryNode node = entry.getValue().build(buildContext);
            contextVariablesMap.put(entry.getKey(), node);
        }

        List<Node> stepList = new ArrayList<>();
        for (NodeBuilder builder : pipes) {
            Node node = builder.build(buildContext);
            stepList.add(node);
        }

        PublishBuilder.PublishNode publishNode = publishBuilder == null ? null : (PublishBuilder.PublishNode) publishBuilder.build(buildContext);

        return (R) new ParallelNode(buildContext.getInstance(SpecificationBuilder.GLOBAL_CONFIGURATION), splitToListQueryNode, contextVariablesMap, stepList, publishNode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ParallelBuilder that = (ParallelBuilder) o;
        return Objects.equals(splitBuilder, that.splitBuilder) &&
                Objects.equals(variables, that.variables) &&
                Objects.equals(pipes, that.pipes) &&
                Objects.equals(publishBuilder, that.publishBuilder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), splitBuilder, variables, pipes, publishBuilder);
    }

    @Override
    public String toString() {
        return "ParallelBuilder{" +
                "splitBuilder=" + splitBuilder +
                ", variables=" + variables +
                ", pipes=" + pipes +
                ", publishBuilder=" + publishBuilder +
                '}';
    }

    static class ParallelNode extends FlowNode implements Parallel {

        final QueryBuilder.QueryNode splitQueryNode;
        final Map<String, QueryBuilder.QueryNode> variables;
        final List<Node> steps;
        final Publish publishNode;

        ParallelNode(Configurations configurations, QueryBuilder.QueryNode splitQueryNode, Map<String, QueryBuilder.QueryNode> variables, List<Node> steps, Publish publishNode) {
            super(configurations);
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
                    "splitQueryNode=" + splitQueryNode +
                    ", variables=" + variables +
                    ", steps=" + steps +
                    ", publishNode=" + publishNode +
                    '}';
        }
    }
}
