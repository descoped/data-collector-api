package no.ssb.dc.api.node.builder;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import no.ssb.dc.api.node.Base;
import no.ssb.dc.api.node.Node;
import no.ssb.dc.api.node.ValidateRequest;

import java.util.Iterator;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class ValidateRequestBuilder extends NodeBuilder {

    public ValidateRequestBuilder() {
        super(BuilderType.ValidateRequest);
    }

    @Override
    <R extends Base> R build(BuildContext buildContext) {
        return (R) new ValidateRequestNode();
    }


    static class ValidateRequestNode extends FlowNode implements ValidateRequest {

        ValidateRequestNode() {
        }

        @Override
        public Iterator<? extends Node> iterator() {
            return createNodeList().iterator();
        }
    }
}
