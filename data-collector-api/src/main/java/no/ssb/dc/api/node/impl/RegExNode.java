package no.ssb.dc.api.node.impl;

import no.ssb.dc.api.node.Query;
import no.ssb.dc.api.node.RegEx;

import java.util.Objects;

public class RegExNode extends QueryNode implements RegEx {

    private final QueryNode queryNode;

    private final String expression;

    public RegExNode(QueryNode queryNode, String expression) {
        this.queryNode = queryNode;
        this.expression = expression;
    }

    @Override
    public Query query() {
        return queryNode;
    }

    @Override
    public String expression() {
        return expression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegExNode)) return false;
        RegExNode regExNode = (RegExNode) o;
        return Objects.equals(queryNode, regExNode.queryNode) &&
                Objects.equals(expression, regExNode.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(queryNode, expression);
    }
    @Override
    public String toString() {
        return "RegExNode{" +
                "query=" + queryNode +
                ", expression='" + expression + '\'' +
                '}';
    }

}
