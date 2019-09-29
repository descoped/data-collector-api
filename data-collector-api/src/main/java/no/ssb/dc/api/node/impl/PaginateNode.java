package no.ssb.dc.api.node.impl;

import no.ssb.dc.api.node.Condition;
import no.ssb.dc.api.node.Execute;
import no.ssb.dc.api.node.Node;
import no.ssb.dc.api.node.Paginate;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class PaginateNode extends OperationNode implements Paginate {

    final Map<String, String> variables;
    final boolean addPageContent;
    final List<Execute> children;
    final double threshold;
    final ConditionNode conditionNode;

    public PaginateNode(String id, Map<String, String> variables, boolean addPageContent, List<Execute> children, double threshold, ConditionNode conditionNode) {
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
