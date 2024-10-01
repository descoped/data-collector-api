package io.descoped.dc.api.node.builder;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.descoped.dc.api.node.Base;
import io.descoped.dc.api.node.Body;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class BodyBuilder extends QueryBuilder {

    public BodyBuilder() {
        super(BuilderType.QueryBody);
    }

    @Override
    <R extends Base> R build(BuildContext buildContext) {
        return (R) new BodyNode();
    }

    @Override
    public String toString() {
        return "BodyBuilder{}";
    }

    class BodyNode extends QueryNode implements Body {

        public BodyNode() {
        }

        // NOP expression - the body query returns unaltered response body
        @Override
        public String expression() {
            return Void.TYPE.getName();
        }

        @Override
        public String toString() {
            return "BodyNode{}";
        }
    }

}
