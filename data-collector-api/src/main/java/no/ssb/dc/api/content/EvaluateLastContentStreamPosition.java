package no.ssb.dc.api.content;

import no.ssb.dc.api.context.ExecutionContext;
import no.ssb.dc.api.health.HealthResourceUtils;

import java.util.Objects;

public class EvaluateLastContentStreamPosition {

    private final ExecutionContext context;

    public EvaluateLastContentStreamPosition(ExecutionContext context) {
        this.context = context;
    }

    public String getLastPosition() {
        String topic = context.state("global.topic");
        Objects.requireNonNull(topic);
        ContentStore contentStore = context.services().get(ContentStore.class);
        Objects.requireNonNull(contentStore);

        /*
         * This call will trigger content-store to seek for lastPosition the first time.
         * The GCS Provider scans all avro segments in bucket in order to resolves lastPosition, which takes unkown amount of time.
         * It is i,portant that we only invoke get lastPosition once during initialization.
         */
        String lastPosition = contentStore.lastPosition(topic);

        HealthResourceUtils.updateMonitorLastPosition(context, lastPosition);

        return lastPosition;
    }
}
