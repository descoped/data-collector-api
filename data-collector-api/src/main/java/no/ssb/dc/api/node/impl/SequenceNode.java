package no.ssb.dc.api.node.impl;

import no.ssb.dc.api.node.Node;
import no.ssb.dc.api.node.Sequence;

import java.util.Iterator;
import java.util.Objects;

public class SequenceNode extends FlowNode implements Sequence {

    final QueryNode splitNode;
    final QueryNode expectedNode;

    public SequenceNode(QueryNode splitNode, QueryNode expectedNode) {
        this.splitNode = splitNode;
        this.expectedNode = expectedNode;
    }

    @Override
    public QueryNode splitToListQuery() {
        return splitNode;
    }

    @Override
    public QueryNode expectedQuery() {
        return expectedNode;
    }

    @Override
    public Iterator<? extends Node> iterator() {
        return createNodeList().iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SequenceNode that = (SequenceNode) o;
        return Objects.equals(splitNode, that.splitNode) &&
                Objects.equals(expectedNode, that.expectedNode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(splitNode, expectedNode);
    }

    @Override
    public String toString() {
        return "SequenceNode{" +
                "splitNode=" + splitNode +
                ", expectedNode=" + expectedNode +
                '}';
    }
}
