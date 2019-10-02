package no.ssb.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import no.ssb.dc.api.node.Base;
import no.ssb.dc.api.node.Node;
import no.ssb.dc.api.node.Publish;

import java.util.Iterator;
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
    <R extends Base> R build(BuildContext buildContext) {
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

    static class PublishNode extends FlowNode implements Publish {

        final String positionVariableExpression;

        PublishNode(String positionVariableExpression) {
            this.positionVariableExpression = positionVariableExpression;
        }

        @Override
        public String positionVariableExpression() {
            return positionVariableExpression;
        }

        @Override
        public Iterator<? extends Node> iterator() {
            return createNodeList().iterator();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PublishNode that = (PublishNode) o;
            return Objects.equals(positionVariableExpression, that.positionVariableExpression);
        }

        @Override
        public int hashCode() {
            return Objects.hash(positionVariableExpression);
        }

        @Override
        public String toString() {
            return "PublishNode{" +
                    "positionVariableExpression='" + positionVariableExpression + '\'' +
                    '}';
        }

    }
}
