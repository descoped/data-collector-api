package no.ssb.dc.api.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import no.ssb.dc.api.BuilderType;
import no.ssb.dc.api.NodeBuilderDeserializer;
import no.ssb.dc.api.node.BaseNode;
import no.ssb.dc.api.node.impl.PublishNode;

import java.util.Map;
import java.util.Objects;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class PublishBuilder extends NodeBuilder {

    @JsonProperty String positionVariableExpression;

    public PublishBuilder(String positionVariableExpression) {
        super(BuilderType.Publish);
        this.positionVariableExpression = positionVariableExpression;
    }

    @SuppressWarnings("unchecked")
    @Override
    <R extends BaseNode> R build(Map<String, NodeBuilder> nodeBuilderById, Map<String, R> nodeInstanceById) {
        return (R) new PublishNode(positionVariableExpression);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PublishBuilder)) return false;
        PublishBuilder that = (PublishBuilder) o;
        return Objects.equals(positionVariableExpression, that.positionVariableExpression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(positionVariableExpression);
    }

    @Override
    public String toString() {
        return "PublishBuilder{" +
                "positionVariableExpression='" + positionVariableExpression + '\'' +
                '}';
    }
}
