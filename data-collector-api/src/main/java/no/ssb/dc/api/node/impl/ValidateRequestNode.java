package no.ssb.dc.api.node.impl;

import no.ssb.dc.api.node.Node;
import no.ssb.dc.api.node.ValidateRequest;

import java.util.Iterator;

public class ValidateRequestNode extends FlowNode implements ValidateRequest {

    public ValidateRequestNode() {
    }

    @Override
    public Iterator<? extends Node> iterator() {
        return createNodeList().iterator();
    }
}
