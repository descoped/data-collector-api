package no.ssb.dc.api.content;

import no.ssb.dc.api.context.ExecutionContext;
import no.ssb.dc.api.el.EvaluationException;
import no.ssb.dc.api.health.HealthResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvaluateLastContentStreamPosition {

    private static final Logger LOG = LoggerFactory.getLogger(EvaluateLastContentStreamPosition.class);

    private final ExecutionContext context;

    public EvaluateLastContentStreamPosition(ExecutionContext context) {
        this.context = context;
    }

    public String getLastPosition() {
        try {
            String topic = context.state("global.topic");
            if (topic == null || "".equals(topic)) {
                throw new EvaluationException("Topic is null!");
            }
            ContentStore contentStore = context.services().get(ContentStore.class);
            if (contentStore == null || contentStore.isClosed()) {
                throw new EvaluationException("ContentStore is null or closed!");
            }
            /*
             * This call will trigger content-store to seek for lastPosition the first time.
             * The GCS Provider scans all avro segments in bucket in order to resolves lastPosition, which takes unkown amount of time.
             * It is i,portant that we only invoke get lastPosition once during initialization.
             */
            String lastPosition = contentStore.lastPosition(topic);

            HealthResourceUtils.updateMonitorLastPosition(context, lastPosition);

            return lastPosition;

        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Exception e) {
            throw new EvaluationException(e);
        }
    }
}
