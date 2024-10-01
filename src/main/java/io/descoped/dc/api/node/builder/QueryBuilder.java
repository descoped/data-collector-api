package io.descoped.dc.api.node.builder;

import io.descoped.dc.api.node.Query;

public abstract class QueryBuilder extends NodeBuilder {

    QueryBuilder(BuilderType type) {
        super(type);
    }

    abstract static class QueryNode extends AbstractBaseNode implements Query {
    }
}
