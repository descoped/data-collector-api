package io.descoped.dc.api.node.builder;


import io.descoped.dc.api.node.ResponsePredicate;

public abstract class ResponsePredicateBuilder extends LeafNodeBuilder {

    ResponsePredicateBuilder(BuilderType type) {
        super(type);
    }

    abstract static class ResponsePredicateNode extends AbstractBaseNode implements ResponsePredicate {
    }
}
