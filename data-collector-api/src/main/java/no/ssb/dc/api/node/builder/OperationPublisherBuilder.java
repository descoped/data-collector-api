package no.ssb.dc.api.node.builder;

import no.ssb.dc.api.node.OperationPublisher;

public abstract class OperationPublisherBuilder extends LeafNodeBuilder {

    OperationPublisherBuilder(BuilderType type) {
        super(type);
    }

    abstract static class OperationPublisherNode extends AbstractBaseNode implements OperationPublisher {
    }
}
