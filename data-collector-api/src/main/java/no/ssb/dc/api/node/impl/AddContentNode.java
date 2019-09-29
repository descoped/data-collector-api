package no.ssb.dc.api.node.impl;

import no.ssb.dc.api.node.AddContent;
import no.ssb.dc.api.node.Node;

import java.util.Iterator;
import java.util.Objects;

public class AddContentNode extends FlowNode implements AddContent {

    final String positionVariableExpression;
    final String contentKey;

    public AddContentNode(String positionVariableExpression, String contentKey) {
        this.positionVariableExpression = positionVariableExpression;
        this.contentKey = contentKey;
    }

    @Override
    public String positionVariableExpression() {
        return positionVariableExpression;
    }

    @Override
    public String contentKey() {
        return contentKey;
    }

    @Override
    public Iterator<? extends Node> iterator() {
        return createNodeList().iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddContentNode that = (AddContentNode) o;
        return positionVariableExpression.equals(that.positionVariableExpression) &&
                contentKey.equals(that.contentKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(positionVariableExpression, contentKey);
    }

    @Override
    public String toString() {
        return "AddContentNode{" +
                "positionVariableExpression='" + positionVariableExpression + '\'' +
                ", contentKey='" + contentKey + '\'' +
                '}';
    }
}
