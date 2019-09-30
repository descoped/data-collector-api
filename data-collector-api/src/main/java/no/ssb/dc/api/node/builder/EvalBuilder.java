package no.ssb.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import no.ssb.dc.api.node.BaseNode;
import no.ssb.dc.api.node.Eval;
import no.ssb.dc.api.node.Query;

import java.util.Map;
import java.util.Objects;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class EvalBuilder extends QueryBuilder {

    @JsonProperty("query") QueryBuilder queryBuilder;
    @JsonProperty("bindToVariable") String bindToVariable;
    @JsonProperty("elExpression") String elExpression;

    public EvalBuilder(QueryBuilder queryBuilder, String bindToVariable, String elExpression) {
        super(BuilderType.QueryEval);
        this.queryBuilder = queryBuilder;
        this.bindToVariable = bindToVariable;
        this.elExpression = elExpression;
    }


    @Override
    <R extends BaseNode> R build(Map<String, NodeBuilder> nodeBuilderById, Map<String, R> nodeInstanceById) {
        QueryNode queryNode = (QueryNode) queryBuilder.build(nodeBuilderById, nodeInstanceById);
        return (R) new EvalNode(queryNode, bindToVariable, elExpression);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        EvalBuilder that = (EvalBuilder) o;
        return queryBuilder.equals(that.queryBuilder) &&
                bindToVariable.equals(that.bindToVariable) &&
                elExpression.equals(that.elExpression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), queryBuilder, bindToVariable, elExpression);
    }

    static class EvalNode extends QueryNode implements Eval {

        final Query query;
        final String bindToVariable;
        final String expression;

        EvalNode(Query query, String bindToVariable, String expression) {

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
}
