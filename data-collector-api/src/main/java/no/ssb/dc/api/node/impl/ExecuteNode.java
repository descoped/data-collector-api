package no.ssb.dc.api.node.impl;

import no.ssb.dc.api.node.Execute;
import no.ssb.dc.api.node.Node;
import no.ssb.dc.api.node.Operation;
import no.ssb.dc.api.node.Query;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ExecuteNode extends FlowNode implements Execute {

    final String executeId;
    final List<String> requiredInputs;
    final Map<String, QueryNode> inputVariables;
    final OperationNode targetNode;

    public ExecuteNode(String executeId, List<String> requiredInputs, Map<String, QueryNode> inputVariables, OperationNode targetNode) {
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
    public Operation target() {
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
        return Objects.equals(executeId, that.executeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(executeId);
    }

    @Override
    public String toString() {
        return "ExecuteNode{" +
                "executeId='" + executeId + '\'' +
                ", requiredInputs=" + requiredInputs +
                ", inputVariables=" + inputVariables +
                ", executeNode=" + targetNode +
                '}';
    }
}
