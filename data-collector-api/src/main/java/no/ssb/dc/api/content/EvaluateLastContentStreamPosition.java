package no.ssb.dc.api.content;

import no.ssb.dc.api.context.ExecutionContext;

public class EvaluateLastContentStreamPosition {

    private final String lastPosition;

    public EvaluateLastContentStreamPosition(ExecutionContext context) {
        String topic = context.state("global.topic");
        ContentStore contentStore = context.services().get(ContentStore.class);
        lastPosition = contentStore.lastPosition(topic);
    }

    public String getLastPosition() {
        return lastPosition;
    }
}
