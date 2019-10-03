package no.ssb.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import no.ssb.dc.api.node.AddContent;
import no.ssb.dc.api.node.Base;
import no.ssb.dc.api.node.Configurations;
import no.ssb.dc.api.node.Node;

import java.util.Iterator;
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
    <R extends Base> R build(BuildContext buildContext) {
        return (R) new AddContentNode(buildContext.getInstance(FlowBuilder.GLOBAL_CONFIGURATION), positionVariableExpression, contentKey);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
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

    static class AddContentNode extends FlowNode implements AddContent {

        final String positionVariableExpression;
        final String contentKey;

        AddContentNode(Configurations configurations, String positionVariableExpression, String contentKey) {
            super(configurations);
            this.positionVariableExpression = positionVariableExpression;
            this.contentKey = contentKey;
        }

        @Override
        public String positionVariableExpression() {
            return positionVariableExpression;
        }

        @Override
        public String contentKey() {
            return contentKey;
        }

        @Override
        public Iterator<? extends Node> iterator() {
            return createNodeList().iterator();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AddContentNode that = (AddContentNode) o;
            return positionVariableExpression.equals(that.positionVariableExpression) &&
                    contentKey.equals(that.contentKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(positionVariableExpression, contentKey);
        }

        @Override
        public String toString() {
            return "AddContentNode{" +
                    "positionVariableExpression='" + positionVariableExpression + '\'' +
                    ", contentKey='" + contentKey + '\'' +
                    '}';
        }
    }
}
