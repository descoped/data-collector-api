package no.ssb.dc.api.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import no.ssb.dc.api.BuilderType;
import no.ssb.dc.api.NodeBuilderDeserializer;
import no.ssb.dc.api.node.BaseNode;
import no.ssb.dc.api.node.impl.AddContentNode;

import java.util.Map;
import java.util.Objects;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class AddContentBuilder extends NodeBuilder {

    @JsonProperty String positionVariableExpression;

    @JsonProperty String contentKey;

    public AddContentBuilder(String positionVariableExpression, String contentKey) {
        super(BuilderType.AddContent);
        this.positionVariableExpression = positionVariableExpression;
        this.contentKey = contentKey;
    }

    @SuppressWarnings("unchecked")
    @Override
    <R extends BaseNode> R build(Map<String, NodeBuilder> nodeBuilderById, Map<String, R> nodeInstanceById) {
        return (R) new AddContentNode(positionVariableExpression, contentKey);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddContentBuilder that = (AddContentBuilder) o;
        return positionVariableExpression.equals(that.positionVariableExpression) &&
                contentKey.equals(that.contentKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), positionVariableExpression, contentKey);
    }

    @Override
    public String toString() {
        return "AddContentBuilder{" +
                "positionVariableExpression='" + positionVariableExpression + '\'' +
                ", contentKey='" + contentKey + '\'' +
                '}';
    }
}
