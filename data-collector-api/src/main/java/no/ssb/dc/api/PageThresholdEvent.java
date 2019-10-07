package no.ssb.dc.api;

import java.util.function.Consumer;

public class PageThresholdEvent {

    private final double threshold;
    private final Consumer<PageContext> preFetchNextPageCallback;

    public PageThresholdEvent(double threshold, Consumer<PageContext> preFetchNextPageCallback) {
        this.threshold = threshold;
        this.preFetchNextPageCallback = preFetchNextPageCallback;
    }

    public double getThreshold() {
        return threshold;
    }

    public Consumer<PageContext> preFetchNextPageCallback() {
        return preFetchNextPageCallback;
    }
}
