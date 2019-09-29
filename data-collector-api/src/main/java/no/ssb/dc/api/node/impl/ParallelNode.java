package no.ssb.dc.api.node.impl;

import no.ssb.dc.api.node.Node;
import no.ssb.dc.api.node.Parallel;
import no.ssb.dc.api.node.Publish;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ParallelNode extends FlowNode implements Parallel {

    final QueryNode splitQueryNode;
    final Map<String, QueryNode> variables;
    final List<Node> steps;
    final Publish publishNode;

    public ParallelNode(QueryNode splitQueryNode, Map<String, QueryNode> variables, List<Node> steps, Publish publishNode) {
        this.splitQueryNode = splitQueryNode;
        this.variables = variables;
        this.steps = steps;
        this.publishNode = publishNode;
    }

    @Override
    public QueryNode splitQuery() {
        return splitQueryNode;
    }

    @Override
    public Set<String> variableNames() {
        return variables.keySet();
    }

    @Override
    public QueryNode variable(String name) {
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
