package io.descoped.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.descoped.dc.api.node.Base;
import io.descoped.dc.api.node.BodyContains;
import io.descoped.dc.api.node.Query;

import java.util.Objects;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class BodyContainsBuilder extends ResponsePredicateBuilder {

    @JsonProperty
    QueryBuilder queryBuilder;
    @JsonProperty
    String equalToStringLiteral;

    public BodyContainsBuilder(QueryBuilder queryBuilder, String equalToStringLiteral) {
        super(BuilderType.HttpResponseBodyContains);
        this.queryBuilder = queryBuilder;
        this.equalToStringLiteral = equalToStringLiteral;
    }

    @Override
    public <R extends Base> R build(BuildContext buildContext) {
        Query query = queryBuilder.build(buildContext);
        return (R) new BodyContainsNode(query, equalToStringLiteral);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BodyContainsBuilder that = (BodyContainsBuilder) o;
        return Objects.equals(queryBuilder, that.queryBuilder) &&
                Objects.equals(equalToStringLiteral, that.equalToStringLiteral);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), queryBuilder, equalToStringLiteral);
    }

    @Override
    public String toString() {
        return "BodyContainsBuilder{" +
                "queryBuilder=" + queryBuilder +
                ", equalToStringLiteral='" + equalToStringLiteral + '\'' +
                '}';
    }

    class BodyContainsNode extends LeafNode implements BodyContains {

        private final Query query;
        private final String equalToStringLiteral;

        public BodyContainsNode(Query query, String equalToStringLiteral) {
            this.query = query;
            this.equalToStringLiteral = equalToStringLiteral;
        }

        @Override
        public Query getQuery() {
            return query;
        }

        @Override
        public String getEqualToStringLiteral() {
            return equalToStringLiteral;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BodyContainsNode that = (BodyContainsNode) o;
            return Objects.equals(query, that.query) &&
                    Objects.equals(equalToStringLiteral, that.equalToStringLiteral);
        }

        @Override
        public int hashCode() {
            return Objects.hash(query, equalToStringLiteral);
        }

        @Override
        public String toString() {
            return "BodyContainsNode{" +
                    "query=" + query +
                    ", equalToStringLiteral='" + equalToStringLiteral + '\'' +
                    '}';
        }
    }

}
