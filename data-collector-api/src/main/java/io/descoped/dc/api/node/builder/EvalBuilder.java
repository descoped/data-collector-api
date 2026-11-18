package io.descoped.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.descoped.dc.api.node.Base;
import io.descoped.dc.api.node.Eval;
import io.descoped.dc.api.node.Query;

import java.util.Objects;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class EvalBuilder extends QueryBuilder {

    @JsonProperty("query")
    QueryBuilder queryBuilder;
    @JsonProperty("bindToVariable")
    String bindToVariable;
    @JsonProperty("expression")
    String elExpression;

    public EvalBuilder(String elExpression) {
        super(BuilderType.QueryEval);
        this.elExpression = elExpression;
    }

    public EvalBuilder(QueryBuilder queryBuilder, String bindToVariable, String elExpression) {
        super(BuilderType.QueryEval);
        this.queryBuilder = queryBuilder;
        this.bindToVariable = bindToVariable;
        this.elExpression = elExpression;
    }

    @Override
    <R extends Base> R build(BuildContext buildContext) {
        QueryNode queryNode = queryBuilder != null ? queryBuilder.build(buildContext) : null;
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

    @Override
    public String toString() {
        return "EvalBuilder{" +
                "queryBuilder=" + queryBuilder +
                ", bindToVariable='" + bindToVariable + '\'' +
                ", elExpression='" + elExpression + '\'' +
                '}';
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
        public String toString() {
            return "EvalNode{" +
                    "query=" + query +
                    ", bindToVariable='" + bindToVariable + '\'' +
                    ", expression='" + expression + '\'' +
                    '}';
        }
    }
}
