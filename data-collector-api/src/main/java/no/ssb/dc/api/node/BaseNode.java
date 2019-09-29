package no.ssb.dc.api.node;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

// used by all simple nodes
public interface BaseNode {

    boolean instanceOf(Class<? extends BaseNode> nodeClass);

    <R extends BaseNode> R cast(Class<R> nodeClass);

    <R extends BaseNode> void given(Class<R> isInstanceOfNodeClass, Consumer<R> then);

    <R extends BaseNode> void given(Class<R> isInstanceOfNodeClass, AtomicBoolean andIfNotHandled, Consumer<R> then);

}
