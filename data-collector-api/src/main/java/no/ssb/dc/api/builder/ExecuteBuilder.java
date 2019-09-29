package no.ssb.dc.api.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import no.ssb.dc.api.BuilderType;
import no.ssb.dc.api.NodeBuilderDeserializer;
import no.ssb.dc.api.node.BaseNode;
import no.ssb.dc.api.node.impl.ExecuteNode;
import no.ssb.dc.api.node.impl.OperationNode;
import no.ssb.dc.api.node.impl.QueryNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class ExecuteBuilder extends NodeBuilder {

    @JsonProperty String executeId;
    @JsonProperty List<String> requiredInputs = new ArrayList<>();
    @JsonProperty Map<String, QueryBuilder> inputVariables = new LinkedHashMap<>();

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
    <R extends BaseNode> R build(Map<String, NodeBuilder> nodeBuilderById, Map<String, R> nodeInstanceById) {
        Map<String, QueryNode> inputVariableMap = inputVariables.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (QueryNode) e.getValue().build(nodeBuilderById, nodeInstanceById)));

        if (!nodeBuilderById.containsKey(executeId)) {
            throw new RuntimeException("Builder" + this.getClass() + " points to an undefined node: " + this.executeId);
        }

        OperationNode targetExecuteNode = (OperationNode) (nodeInstanceById.containsKey(executeId) ?
                nodeInstanceById.get(executeId) :
                nodeBuilderById.get(executeId).build(nodeBuilderById, nodeInstanceById));

        nodeInstanceById.computeIfAbsent(executeId, node -> (R) targetExecuteNode);

        return (R) new ExecuteNode(executeId, requiredInputs, inputVariableMap, targetExecuteNode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExecuteBuilder)) return false;
        ExecuteBuilder that = (ExecuteBuilder) o;
        return Objects.equals(executeId, that.executeId) &&
                Objects.equals(requiredInputs, that.requiredInputs) &&
                Objects.equals(inputVariables, that.inputVariables);
    }

    @Override
    public int hashCode() {
        return Objects.hash(executeId, requiredInputs, inputVariables);
    }

    @Override
    public String toString() {
        return "ExecuteBuilder{" +
                "executeId='" + executeId + '\'' +
                ", requiredInputs=" + requiredInputs +
                ", inputVariables=" + inputVariables +
                '}';
    }
}
