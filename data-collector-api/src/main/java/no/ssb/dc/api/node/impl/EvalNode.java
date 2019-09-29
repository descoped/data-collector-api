package no.ssb.dc.api.node.impl;

import no.ssb.dc.api.node.Eval;
import no.ssb.dc.api.node.Query;

import java.util.Objects;

public class EvalNode extends QueryNode implements Eval {

    final Query query;
    final String bindToVariable;
    final String expression;

    public EvalNode(Query query, String bindToVariable, String expression) {

        this.query = query;
        this.bindToVariable = bindToVariable;
        this.expression = expression;
    }

    @Override
    public Query query() {
        return query;
    }

    @Override
    public String bind() {
        return bindToVariable;
    }

    @Override
    public String expression() {
        return expression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EvalNode evalNode = (EvalNode) o;
        return query.equals(evalNode.query) &&
                bindToVariable.equals(evalNode.bindToVariable) &&
                expression.equals(evalNode.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(query, bindToVariable, expression);
    }

    @Override
    public String toString() {
        return "EvalNode{" +
                "query=" + query +
                ", bindToVariable='" + bindToVariable + '\'' +
                ", expression='" + expression + '\'' +
                '}';
    }
}
