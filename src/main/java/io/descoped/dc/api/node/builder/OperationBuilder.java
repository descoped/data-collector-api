package io.descoped.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.descoped.dc.api.node.Configurations;
import io.descoped.dc.api.node.Operation;

public abstract class OperationBuilder extends NodeWithIdBuilder {

    @JsonProperty
    String url;

    OperationBuilder(BuilderType type) {
        super(type);
    }

    abstract static class OperationNode extends FlowNodeWithId implements Operation {
        OperationNode(Configurations configurations, String id) {
            super(configurations, id);
        }
    }
}
