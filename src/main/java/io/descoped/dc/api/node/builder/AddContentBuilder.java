package io.descoped.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.descoped.dc.api.node.AddContent;
import io.descoped.dc.api.node.Base;
import io.descoped.dc.api.node.Configurations;
import io.descoped.dc.api.node.Node;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class AddContentBuilder extends NodeBuilder {

    @JsonProperty
    String positionVariableExpression;

    @JsonProperty
    String contentKey;

    @JsonProperty
    Map<String, Object> state = new LinkedHashMap<>();

    public AddContentBuilder(String positionVariableExpression, String contentKey) {
        super(BuilderType.AddContent);
        this.positionVariableExpression = positionVariableExpression;
        this.contentKey = contentKey;
    }

    public AddContentBuilder storeState(String key, Object value) {
        state.put(key, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    <R extends Base> R build(BuildContext buildContext) {
        return (R) new AddContentNode(buildContext.getInstance(SpecificationBuilder.GLOBAL_CONFIGURATION), positionVariableExpression, contentKey, state);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AddContentBuilder that = (AddContentBuilder) o;
        return Objects.equals(positionVariableExpression, that.positionVariableExpression) &&
                Objects.equals(contentKey, that.contentKey) &&
                Objects.equals(state, that.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), positionVariableExpression, contentKey, state);
    }

    @Override
    public String toString() {
        return "AddContentBuilder{" +
                "positionVariableExpression='" + positionVariableExpression + '\'' +
                ", contentKey='" + contentKey + '\'' +
                ", state=" + state +
                '}';
    }

    static class AddContentNode extends FlowNode implements AddContent {

        final String positionVariableExpression;
        final String contentKey;
        final Map<String, Object> state;

        AddContentNode(Configurations configurations, String positionVariableExpression, String contentKey, Map<String, Object> state) {
            super(configurations);
            this.positionVariableExpression = positionVariableExpression;
            this.contentKey = contentKey;
            this.state = state;
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
        public Map<String, Object> state() {
            return state;
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
            return Objects.equals(positionVariableExpression, that.positionVariableExpression) &&
                    Objects.equals(contentKey, that.contentKey) &&
                    Objects.equals(state, that.state);
        }

        @Override
        public int hashCode() {
            return Objects.hash(positionVariableExpression, contentKey, state);
        }

        @Override
        public String toString() {
            return "AddContentNode{" +
                    "positionVariableExpression='" + positionVariableExpression + '\'' +
                    ", contentKey='" + contentKey + '\'' +
                    ", state=" + state +
                    '}';
        }
    }
}
