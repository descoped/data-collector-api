package no.ssb.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import no.ssb.dc.api.node.Base;
import no.ssb.dc.api.node.Condition;
import no.ssb.dc.api.node.Execute;
import no.ssb.dc.api.node.Node;
import no.ssb.dc.api.node.Paginate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class PaginateBuilder extends OperationBuilder {

    @JsonProperty Map<String, String> variables = new LinkedHashMap<>();
    @JsonProperty List<ExecuteBuilder> children = new ArrayList<>();
    @JsonProperty double threshold;
    @JsonProperty("until") ConditionBuilder conditionBuilder;
    @JsonProperty boolean addPageContent;

    PaginateBuilder() {
        super(BuilderType.Paginate);
    }

    public PaginateBuilder(String id) {
        super(BuilderType.Paginate);
        setId(id);
    }

    public PaginateBuilder variable(String identifier, String expression) {
        variables.put(identifier, expression);
        return this;
    }

    public PaginateBuilder step(ExecuteBuilder executeBuilder) {
        children.add(executeBuilder);
        return this;
    }

    public PaginateBuilder prefetchThreshold(double threshold) {
        this.threshold = threshold;
        return this;
    }

    public PaginateBuilder until(ConditionBuilder conditionBuilder) {
        this.conditionBuilder = conditionBuilder;
        return this;
    }

    public PaginateBuilder addPageContent() {
        this.addPageContent = true;
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    <R extends Base> R build(Map<String, NodeBuilder> nodeBuilderById, Map<String, R> nodeInstanceById) {
        List<Execute> executeNodeList = new ArrayList<>();

        for (ExecuteBuilder executeBuilder : children) {
            Execute executeNode = (Execute) executeBuilder.build(nodeBuilderById, nodeInstanceById);
            executeNodeList.add(executeNode);
        }

        ConditionBuilder.ConditionNode conditionNode = (ConditionBuilder.ConditionNode) conditionBuilder.build(nodeBuilderById, nodeInstanceById);

        return (R) new PaginateNode(getId(), variables, addPageContent, executeNodeList, threshold, conditionNode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PaginateBuilder builder = (PaginateBuilder) o;
        return Double.compare(builder.threshold, threshold) == 0 &&
                addPageContent == builder.addPageContent &&
                Objects.equals(variables, builder.variables) &&
                Objects.equals(children, builder.children) &&
                Objects.equals(conditionBuilder, builder.conditionBuilder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), variables, children, threshold, conditionBuilder, addPageContent);
    }

    @Override
    public String toString() {
        return "PaginateBuilder{" +
                "id='" + id + '\'' +
                ", variables=" + variables +
                ", children=" + children +
                ", threshold=" + threshold +
                ", conditionBuilder=" + conditionBuilder +
                ", addPageContent=" + addPageContent +
                '}';
    }

    static class PaginateNode extends OperationNode implements Paginate {

        final Map<String, String> variables;
        final boolean addPageContent;
        final List<Execute> children;
        final double threshold;
        final ConditionBuilder.ConditionNode conditionNode;

        PaginateNode(String id, Map<String, String> variables, boolean addPageContent, List<Execute> children, double threshold, ConditionBuilder.ConditionNode conditionNode) {
            super(id);
            this.variables = variables;
            this.addPageContent = addPageContent;
            this.children = children;
            this.threshold = threshold;
            this.conditionNode = conditionNode;
        }

        @Override
        public Set<String> variableNames() {
            return variables.keySet();
        }

        @Override
        public String variable(String name) {
            return variables.get(name);
        }

        @Override
        public boolean addPageContent() {
            return addPageContent;
        }

        @Override
        public List<Execute> targets() {
            return children;
        }

        @Override
        public double threshold() {
            return threshold;
        }

        @Override
        public Condition condition() {
            return conditionNode;
        }

        @Override
        public Iterator<? extends Node> iterator() {
            return children.iterator();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PaginateNode that = (PaginateNode) o;
            return addPageContent == that.addPageContent &&
                    Double.compare(that.threshold, threshold) == 0 &&
                    Objects.equals(variables, that.variables) &&
                    Objects.equals(children, that.children) &&
                    Objects.equals(conditionNode, that.conditionNode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(variables, addPageContent, children, threshold, conditionNode);
        }

        @Override
        public String toString() {
            return "PaginateNode{" +
                    "id='" + id + '\'' +
                    ", variables=" + variables +
                    ", addPageContent=" + addPageContent +
                    ", children=" + children +
                    ", threshold=" + threshold +
                    ", conditionNode=" + conditionNode +
                    '}';
        }
    }
}
