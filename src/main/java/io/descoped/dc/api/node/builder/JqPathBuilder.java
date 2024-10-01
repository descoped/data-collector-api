package io.descoped.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.descoped.dc.api.node.Base;
import io.descoped.dc.api.node.JqPath;

import java.util.Objects;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class JqPathBuilder extends QueryBuilder {

    @JsonProperty
    String expression;

    public JqPathBuilder(String expression) {
        super(BuilderType.QueryJqPath);
        this.expression = expression;
    }

    @Override
    <R extends Base> R build(BuildContext buildContext) {
        return (R) new JqPathNode(expression);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        JqPathBuilder builder = (JqPathBuilder) o;
        return expression.equals(builder.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), expression);
    }

    @Override
    public String toString() {
        return "JqPathBuilder{" +
                "expression='" + expression + '\'' +
                '}';
    }

    class JqPathNode extends QueryNode implements JqPath {

        final String expression;

        public JqPathNode(String expression) {
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
            JqPathNode that = (JqPathNode) o;
            return expression.equals(that.expression);
        }

        @Override
        public int hashCode() {
            return Objects.hash(expression);
        }

        @Override
        public String toString() {
            return "JqPathNode{" +
                    "expression='" + expression + '\'' +
                    '}';
        }
    }

}
