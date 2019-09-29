package no.ssb.dc.api.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import no.ssb.dc.api.NodeBuilderDeserializer;
import no.ssb.dc.api.node.BaseNode;
import no.ssb.dc.api.node.impl.XPathNode;

import java.util.Map;
import java.util.Objects;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class XPathBuilder extends QueryBuilder {

    @JsonProperty String expression;

    public XPathBuilder(String expression) {
        super(BuilderType.QueryXPath);
        this.expression = expression;
    }

    @SuppressWarnings("unchecked")
    @Override
    <R extends BaseNode> R build(Map<String, NodeBuilder> nodeBuilderById, Map<String, R> nodeInstanceById) {
        return (R) new XPathNode(expression);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
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
}
