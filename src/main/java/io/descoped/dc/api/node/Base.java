package io.descoped.dc.api.node;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

// used by all simple nodes
public interface Base {

    boolean instanceOf(Class<? extends Base> nodeClass);

    <R extends Base> R cast(Class<R> nodeClass);

    <R extends Base> void given(Class<R> isInstanceOfNodeClass, Consumer<R> then);

    <R extends Base> void given(Class<R> isInstanceOfNodeClass, AtomicBoolean andIfNotHandled, Consumer<R> then);

}
