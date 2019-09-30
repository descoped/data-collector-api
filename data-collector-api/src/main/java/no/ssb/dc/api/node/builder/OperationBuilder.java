package no.ssb.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import no.ssb.dc.api.node.Operation;

public abstract class OperationBuilder extends NodeBuilder {

    @JsonProperty String url;

    OperationBuilder(BuilderType type) {
        super(type);
    }

    abstract static class OperationNode extends FlowNodeWithId implements Operation {
        OperationNode(String id) {
            super(id);
        }
    }
}
