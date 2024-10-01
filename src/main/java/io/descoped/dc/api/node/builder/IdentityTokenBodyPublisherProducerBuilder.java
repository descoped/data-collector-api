package io.descoped.dc.api.node.builder;

abstract public class IdentityTokenBodyPublisherProducerBuilder extends BodyPublisherProducerBuilder {

    IdentityTokenBodyPublisherProducerBuilder(BuilderType type) {
        super(type);
    }


    static abstract class IdentityTokenNode extends LeafNode {

    }
}
