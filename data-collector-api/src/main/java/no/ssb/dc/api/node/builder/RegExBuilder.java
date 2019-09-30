package no.ssb.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import no.ssb.dc.api.node.BaseNode;
import no.ssb.dc.api.node.Query;
import no.ssb.dc.api.node.RegEx;

import java.util.Map;
import java.util.Objects;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class RegExBuilder extends QueryBuilder {

    @JsonProperty("query") QueryBuilder queryBuilder;
    @JsonProperty String expression;

    public RegExBuilder(QueryBuilder queryBuilder, String expression) {
        super(BuilderType.QueryRegEx);
        this.queryBuilder = queryBuilder;
        this.expression = expression;
    }

    @Override
    <R extends BaseNode> R build(Map<String, NodeBuilder> nodeBuilderById, Map<String, R> nodeInstanceById) {
        QueryNode queryNode = (QueryNode) queryBuilder.build(nodeBuilderById, nodeInstanceById);
        return (R) new RegExNode(queryNode, expression);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegExBuilder)) return false;
        RegExBuilder that = (RegExBuilder) o;
        return Objects.equals(queryBuilder, that.queryBuilder) &&
                Objects.equals(expression, that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), queryBuilder, expression);
    }

    @Override
    public String toString() {
        return "RegExBuilder{" +
                "queryBuilder=" + queryBuilder +
                ", expression='" + expression + '\'' +
                '}';
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
}
