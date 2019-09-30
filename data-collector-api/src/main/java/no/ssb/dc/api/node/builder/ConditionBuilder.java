package no.ssb.dc.api.node.builder;

import no.ssb.dc.api.node.Condition;

public abstract class ConditionBuilder extends NodeBuilder {

    ConditionBuilder(BuilderType type) {
        super(type);
    }

    abstract static class ConditionNode extends AbstractBaseNode implements Condition {
    }
}
