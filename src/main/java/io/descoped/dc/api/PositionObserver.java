package io.descoped.dc.api;

import java.util.function.Consumer;

public class PositionObserver {

    private final Consumer<Integer> expectedCallback;
    private final Consumer<Integer> completedCallback;

    public PositionObserver(Consumer<Integer> expectedCallback, Consumer<Integer> completedCallback) {
        this.expectedCallback = expectedCallback;
        this.completedCallback = completedCallback;
    }

    public void expected(int count) {
        expectedCallback.accept(count);
    }

    public void completed(int count) {
        completedCallback.accept(count);
    }
}
