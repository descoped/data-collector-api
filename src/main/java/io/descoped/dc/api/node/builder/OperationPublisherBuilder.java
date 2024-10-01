package io.descoped.dc.api.node.builder;

import io.descoped.dc.api.node.OperationPublisher;

public abstract class OperationPublisherBuilder extends LeafNodeBuilder {

    OperationPublisherBuilder(BuilderType type) {
        super(type);
    }

    abstract static class OperationPublisherNode extends AbstractBaseNode implements OperationPublisher {
    }
}
