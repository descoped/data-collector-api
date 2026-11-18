package io.descoped.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.descoped.dc.api.node.Base;
import io.descoped.dc.api.node.WhenExpressionIsTrue;

import java.util.Objects;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class WhenExpressionIsTrueBuilder extends ConditionBuilder {

    @JsonProperty
    String identifier;

    public WhenExpressionIsTrueBuilder() {
        super(BuilderType.ConditionWhenExpressionIsTrue);
    }

    public WhenExpressionIsTrueBuilder identifier(String identifier) {
        this.identifier = identifier;
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    <R extends Base> R build(BuildContext buildContext) {
        return (R) new WhenExpressionIsTrueNode(identifier);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        WhenExpressionIsTrueBuilder that = (WhenExpressionIsTrueBuilder) o;
        return identifier.equals(that.identifier);
    }

    @Override
    public String toString() {
        return "WhenExpressionIsNotTrueBuilder{" +
                "identifier='" + identifier + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), identifier);
    }

    static class WhenExpressionIsTrueNode extends ConditionNode implements WhenExpressionIsTrue {

        final String identifier;

        WhenExpressionIsTrueNode(String identifier) {
            this.identifier = identifier;
        }

        @Override
        public String identifier() {
            return identifier;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WhenExpressionIsTrueNode that = (WhenExpressionIsTrueNode) o;
            return identifier.equals(that.identifier);
        }

        @Override
        public int hashCode() {
            return Objects.hash(identifier);
        }

        @Override
        public String toString() {
            return "WhenExpressionIsNotTrueNode{" +
                    "identifier='" + identifier + '\'' +
                    '}';
        }
    }
}
