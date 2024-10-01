package io.descoped.dc.api.node.builder;

import io.descoped.dc.api.node.Condition;

public abstract class ConditionBuilder extends NodeBuilder {

    ConditionBuilder(BuilderType type) {
        super(type);
    }

    abstract static class ConditionNode extends AbstractBaseNode implements Condition {
    }
}
