package no.ssb.dc.api.node.impl;

import no.ssb.dc.api.node.Node;
import no.ssb.dc.api.node.Publish;

import java.util.Iterator;
import java.util.Objects;

public class PublishNode extends FlowNode implements Publish {

    final String positionVariableExpression;

    public PublishNode(String positionVariableExpression) {
        this.positionVariableExpression = positionVariableExpression;
    }

    @Override
    public String positionVariableExpression() {
        return positionVariableExpression;
    }

    @Override
    public Iterator<? extends Node> iterator() {
        return createNodeList().iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublishNode that = (PublishNode) o;
        return Objects.equals(positionVariableExpression, that.positionVariableExpression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(positionVariableExpression);
    }

    @Override
    public String toString() {
        return "PublishNode{" +
                "positionVariableExpression='" + positionVariableExpression + '\'' +
                '}';
    }

}
