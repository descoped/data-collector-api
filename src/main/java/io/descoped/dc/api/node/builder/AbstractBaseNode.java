package io.descoped.dc.api.node.builder;

import io.descoped.dc.api.node.Base;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Base for node implementation classes
 */
public abstract class AbstractBaseNode implements Base {

    @Override
    public boolean instanceOf(Class<? extends Base> nodeClass) {
        return this.getClass().isAssignableFrom(nodeClass);
    }

    @Override
    public <R extends Base> R cast(Class<R> nodeClass) {
        return nodeClass.cast(this);
    }

    @Override
    public <R extends Base> void given(Class<R> isInstanceOfNodeClass, Consumer<R> then) {
        if (instanceOf(isInstanceOfNodeClass)) {
            then.accept(cast(isInstanceOfNodeClass));
        }
    }

    @Override
    public <R extends Base> void given(Class<R> isInstanceOfNodeClass, AtomicBoolean andIfNotHandled, Consumer<R> then) {
        if (instanceOf(isInstanceOfNodeClass) && !andIfNotHandled.get()) {
            then.accept(cast(isInstanceOfNodeClass));
            andIfNotHandled.set(true);
        }
    }
}
