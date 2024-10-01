package io.descoped.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.descoped.dc.api.node.Base;
import io.descoped.dc.api.node.Configurations;
import io.descoped.dc.api.node.Execute;
import io.descoped.dc.api.node.Node;
import io.descoped.dc.api.node.NodeWithId;
import io.descoped.dc.api.node.Query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class ExecuteBuilder extends NodeBuilder {

    @JsonProperty
    String executeId;
    @JsonProperty
    List<String> requiredInputs = new ArrayList<>();
    @JsonProperty
    Map<String, QueryBuilder> inputVariables = new LinkedHashMap<>();

    public ExecuteBuilder(String executeId) {
        super(BuilderType.Execute);
        this.executeId = executeId;
    }

    public ExecuteBuilder requiredInput(String identifier) {
        requiredInputs.add(identifier);
        return this;
    }

    public ExecuteBuilder inputVariable(String identifier, QueryBuilder queryBuilder) {
        inputVariables.put(identifier, queryBuilder);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    <R extends Base> R build(BuildContext buildContext) {
        Map<String, QueryBuilder.QueryNode> inputVariableMap = inputVariables.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (QueryBuilder.QueryNode) e.getValue().build(buildContext), (x, y) -> y, LinkedHashMap::new));

        if (!buildContext.containsBuilder(executeId)) {
            throw new RuntimeException("Builder" + this.getClass() + " points to an undefined node: " + this.executeId);
        }

        NodeWithIdBuilder.FlowNodeWithId targetExecuteNode = (buildContext.containsInstance(executeId) ?
                buildContext.getInstance(executeId) :
                buildContext.getBuilder(executeId).build(buildContext));

        buildContext.cacheInstanceIfAbsent(executeId, node -> targetExecuteNode);

        return (R) new ExecuteNode(buildContext.getInstance(SpecificationBuilder.GLOBAL_CONFIGURATION), executeId, requiredInputs, inputVariableMap, targetExecuteNode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ExecuteBuilder that = (ExecuteBuilder) o;
        return executeId.equals(that.executeId) &&
                Objects.equals(requiredInputs, that.requiredInputs) &&
                Objects.equals(inputVariables, that.inputVariables);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), executeId, requiredInputs, inputVariables);
    }

    static class ExecuteNode extends FlowNode implements Execute {

        final String executeId;
        final List<String> requiredInputs;
        final Map<String, QueryBuilder.QueryNode> inputVariables;
        final NodeWithIdBuilder.FlowNodeWithId targetNode;

        ExecuteNode(Configurations configurations, String executeId, List<String> requiredInputs, Map<String, QueryBuilder.QueryNode> inputVariables, NodeWithIdBuilder.FlowNodeWithId targetNode) {
            super(configurations);
            if (targetNode == null) {
                throw new IllegalArgumentException("adjacent executeNode is null");
            }
            this.executeId = executeId;
            this.requiredInputs = requiredInputs;
            this.inputVariables = inputVariables;
            this.targetNode = targetNode;
        }

        @Override
        public String executeId() {
            return executeId;
        }

        @Override
        public List<String> requiredInputs() {
            return requiredInputs;
        }

        @Override
        public Map<String, Query> inputVariable() {
            return inputVariables.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> (Query) y, LinkedHashMap::new));
        }

        @Override
        public NodeWithId target() {
            return targetNode;
        }

        @Override
        public Iterator<? extends Node> iterator() {
            List<Node> nodeList = createNodeList();
            nodeList.add(targetNode);
            return nodeList.iterator();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ExecuteNode that = (ExecuteNode) o;
            return executeId.equals(that.executeId) &&
                    Objects.equals(requiredInputs, that.requiredInputs) &&
                    Objects.equals(inputVariables, that.inputVariables) &&
                    targetNode.equals(that.targetNode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(executeId, requiredInputs, inputVariables, targetNode);
        }
    }
}
