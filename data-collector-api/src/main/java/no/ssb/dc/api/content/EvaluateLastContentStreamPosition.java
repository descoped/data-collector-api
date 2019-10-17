package no.ssb.dc.api.content;

import no.ssb.dc.api.context.ExecutionContext;

import java.util.Objects;

public class EvaluateLastContentStreamPosition {

    private final ExecutionContext context;

    public EvaluateLastContentStreamPosition(ExecutionContext context) {
        this.context = context;
    }

    public boolean hasLastPosition() {
        return getLastPosition() != null;
    }

    public String getLastPosition() {
        String topic = context.state("global.topic");
        Objects.requireNonNull(topic);
        ContentStore contentStore = context.services().get(ContentStore.class);
        Objects.requireNonNull(contentStore);
        return contentStore.lastPosition(topic);
    }
}
