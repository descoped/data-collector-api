package no.ssb.dc.api.node.impl;

import no.ssb.dc.api.node.Operation;

public abstract class OperationNode extends FlowNodeWithId implements Operation {
    OperationNode(String id) {
        super(id);
    }
}
