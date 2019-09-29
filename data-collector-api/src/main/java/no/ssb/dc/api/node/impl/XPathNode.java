package no.ssb.dc.api.node.impl;

import no.ssb.dc.api.node.XPath;

import java.util.Objects;

public class XPathNode extends QueryNode implements XPath {

    final String expression;

    public XPathNode(String expression) {
        this.expression = expression;
    }

    @Override
    public String expression() {
        return expression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        XPathNode xPathNode = (XPathNode) o;
        return expression.equals(xPathNode.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression);
    }

    @Override
    public String toString() {
        return "XPathNode{" +
                "expression='" + expression + '\'' +
                '}';
    }
}
