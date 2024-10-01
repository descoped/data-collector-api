package io.descoped.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.descoped.dc.api.node.Base;
import io.descoped.dc.api.node.Query;
import io.descoped.dc.api.node.RegEx;

import java.util.Objects;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class RegExBuilder extends QueryBuilder {

    @JsonProperty("query")
    QueryBuilder queryBuilder;
    @JsonProperty
    String expression;

    public RegExBuilder(QueryBuilder queryBuilder, String expression) {
        super(BuilderType.QueryRegEx);
        this.queryBuilder = queryBuilder;
        this.expression = expression;
    }

    @Override
    <R extends Base> R build(BuildContext buildContext) {
        QueryNode queryNode = queryBuilder.build(buildContext);
        return (R) new RegExNode(queryNode, expression);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RegExBuilder that = (RegExBuilder) o;
        return queryBuilder.equals(that.queryBuilder) &&
                expression.equals(that.expression);
    }

    @Override
    public String toString() {
        return "RegExBuilder{" +
                "queryBuilder=" + queryBuilder +
                ", expression='" + expression + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), queryBuilder, expression);
    }

    static class RegExNode extends QueryNode implements RegEx {

        private final QueryNode queryNode;

        private final String expression;

        RegExNode(QueryNode queryNode, String expression) {
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
            if (o == null || getClass() != o.getClass()) return false;
            RegExNode regExNode = (RegExNode) o;
            return queryNode.equals(regExNode.queryNode) &&
                    expression.equals(regExNode.expression);
        }

        @Override
        public int hashCode() {
            return Objects.hash(queryNode, expression);
        }

        @Override
        public String toString() {
            return "RegExNode{" +
                    "queryNode=" + queryNode +
                    ", expression='" + expression + '\'' +
                    '}';
        }
    }
}
