package io.descoped.dc.api.content;

import io.descoped.dc.api.context.ExecutionContext;
import io.descoped.dc.api.el.EvaluationException;
import io.descoped.dc.api.health.HealthResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvaluateLastContentStreamPosition {

    private static final Logger LOG = LoggerFactory.getLogger(EvaluateLastContentStreamPosition.class);

    private final ExecutionContext context;

    public EvaluateLastContentStreamPosition(ExecutionContext context) {
        this.context = context;
    }

    public String getLastPagePosition() {
        String topic = context.state("global.topic");
        checkIfTopicIsNull(topic);
        return getLastPositionForTopic(topic + "-pages");
    }

    public String getLastPosition() {
        String topic = context.state("global.topic");
        checkIfTopicIsNull(topic);
        return getLastPositionForTopic(topic);
    }

    private void checkIfTopicIsNull(String topic) {
        if (topic == null || "".equals(topic)) {
            throw new EvaluationException("Topic is null!");
        }

    }

    private String getLastPositionForTopic(String topic) {
        try {
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
