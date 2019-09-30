package no.ssb.dc.api.builder;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import no.ssb.dc.api.node.BaseNode;
import no.ssb.dc.api.node.impl.ValidateRequestNode;

import java.util.Map;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class ValidateRequestBuilder extends NodeBuilder {

    public ValidateRequestBuilder() {
        super(BuilderType.ValidateRequest);
    }

    @Override
    <R extends BaseNode> R build(Map<String, NodeBuilder> nodeBuilderById, Map<String, R> nodeInstanceById) {
        return (R) new ValidateRequestNode();
    }


}
