package io.descoped.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.descoped.dc.api.node.Base;
import io.descoped.dc.api.node.XPath;

import java.util.Objects;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class XPathBuilder extends QueryBuilder {

    @JsonProperty
    String expression;

    public XPathBuilder(String expression) {
        super(BuilderType.QueryXPath);
        this.expression = expression;
    }

    @SuppressWarnings("unchecked")
    @Override
    <R extends Base> R build(BuildContext buildContext) {
        return (R) new XPathNode(expression);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        XPathBuilder that = (XPathBuilder) o;
        return expression.equals(that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), expression);
    }

    @Override
    public String toString() {
        return "XPathBuilder{" +
                "expression='" + expression + '\'' +
                '}';
    }

    class XPathNode extends QueryNode implements XPath {

        final String expression;

        XPathNode(String expression) {
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
}
