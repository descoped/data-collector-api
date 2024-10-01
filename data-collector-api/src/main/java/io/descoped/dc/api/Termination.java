package io.descoped.dc.api;

import java.util.concurrent.atomic.AtomicBoolean;

public class Termination {

    private final AtomicBoolean terminated = new AtomicBoolean(false);

    private Termination() {
    }

    public void terminate() {
        terminated.set(true);
    }

    public boolean isTerminated() {
        return terminated.get();
    }

    public static Termination create() {
        return new Termination();
    }
}
