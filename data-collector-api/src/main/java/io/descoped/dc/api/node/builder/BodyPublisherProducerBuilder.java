package io.descoped.dc.api.node.builder;

abstract public class BodyPublisherProducerBuilder extends LeafNodeBuilder {

    BodyPublisherProducerBuilder(BuilderType type) {
        super(type);
    }

    abstract static class BodyPublisherProducerNode extends LeafNode {
    }

}
