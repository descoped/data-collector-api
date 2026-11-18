package io.descoped.dc.api.node.builder;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.descoped.dc.api.node.Base;
import io.descoped.dc.api.node.JsonToken;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class JsonTokenBuilder extends QueryBuilder {

    public JsonTokenBuilder() {
        super(BuilderType.QueryJsonToken);
    }

    @Override
    <R extends Base> R build(BuildContext buildContext) {
        return (R) new JsonTokenNode();
    }

    @Override
    public String toString() {
        return "JsonTokenBuilder{}";
    }

    static class JsonTokenNode extends QueryBuilder.QueryNode implements JsonToken {

        public JsonTokenNode() {
        }

        // NOP expression - the body query returns unaltered response body
        @Override
        public String expression() {
            return Void.TYPE.getName();
        }

        @Override
        public String toString() {
            return "JsonTokenNode{}";
        }
    }
}
