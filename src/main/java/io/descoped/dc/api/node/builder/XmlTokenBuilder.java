package io.descoped.dc.api.node.builder;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.descoped.dc.api.node.Base;
import io.descoped.dc.api.node.XmlToken;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class XmlTokenBuilder extends QueryBuilder {

    public XmlTokenBuilder() {
        super(BuilderType.QueryXmlToken);
    }

    @Override
    <R extends Base> R build(BuildContext buildContext) {
        return (R) new XmlTokenNode();
    }

    @Override
    public String toString() {
        return "XmlTokenBuilder{}";
    }

    static class XmlTokenNode extends QueryNode implements XmlToken {

        public XmlTokenNode() {
        }

        // NOP expression - the body query returns unaltered response body
        @Override
        public String expression() {
            return Void.TYPE.getName();
        }

        @Override
        public String toString() {
            return "XmlTokenBuilder{}";
        }
    }
}
