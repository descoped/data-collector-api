package no.ssb.dc.api.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import no.ssb.dc.api.NodeBuilderDeserializer;
import no.ssb.dc.api.node.BaseNode;
import no.ssb.dc.api.node.Execute;
import no.ssb.dc.api.node.impl.ConditionNode;
import no.ssb.dc.api.node.impl.PaginateNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    <R extends BaseNode> R build(Map<String, NodeBuilder> nodeBuilderById, Map<String, R> nodeInstanceById) {
        List<Execute> executeNodeList = new ArrayList<>();

        for (ExecuteBuilder executeBuilder : children) {
            Execute executeNode = (Execute) executeBuilder.build(nodeBuilderById, nodeInstanceById);
            executeNodeList.add(executeNode);
        }

        ConditionNode conditionNode = (ConditionNode) conditionBuilder.build(nodeBuilderById, nodeInstanceById);

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
}
