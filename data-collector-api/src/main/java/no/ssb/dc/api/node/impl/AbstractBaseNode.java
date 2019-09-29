package no.ssb.dc.api.node.impl;

import no.ssb.dc.api.node.BaseNode;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public abstract class AbstractBaseNode implements BaseNode {

    @Override
    public boolean instanceOf(Class<? extends BaseNode> nodeClass) {
        return this.getClass().isAssignableFrom(nodeClass);
    }

    @Override
    public <R extends BaseNode> R cast(Class<R> nodeClass) {
        return nodeClass.cast(this);
    }

    @Override
    public <R extends BaseNode> void given(Class<R> isInstanceOfNodeClass, Consumer<R> then) {
        if (instanceOf(isInstanceOfNodeClass)) {
            then.accept(cast(isInstanceOfNodeClass));
        }
    }

    @Override
    public <R extends BaseNode> void given(Class<R> isInstanceOfNodeClass, AtomicBoolean andIfNotHandled, Consumer<R> then) {
        if (instanceOf(isInstanceOfNodeClass) && !andIfNotHandled.get()) {
            then.accept(cast(isInstanceOfNodeClass));
            andIfNotHandled.set(true);
        }
    }
}
