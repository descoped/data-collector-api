package no.ssb.dc.api.node.impl;

import no.ssb.dc.api.node.NodeWithId;

public abstract class FlowNodeWithId extends FlowNode implements NodeWithId {
    final String id;

    FlowNodeWithId(String id) {
        this.id = id;
    }

    @Override
    public String id() {
        return id;
    }
}
