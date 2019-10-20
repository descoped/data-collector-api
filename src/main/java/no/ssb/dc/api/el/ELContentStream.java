package no.ssb.dc.api.el;

import no.ssb.dc.api.content.EvaluateLastContentStreamPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ELContentStream {
    private static final Logger LOG = LoggerFactory.getLogger(ELContentStream.class);
    private final EvaluateLastContentStreamPosition evaluateLastContentStreamPosition;

    ELContentStream(EvaluateLastContentStreamPosition evaluateLastContentStreamPosition) {
        this.evaluateLastContentStreamPosition = evaluateLastContentStreamPosition;
    }

    public boolean hasLastPosition() {
        return evaluateLastContentStreamPosition.hasLastPosition();
    }

    public String lastPosition() {
        String lastPosition = evaluateLastContentStreamPosition.getLastPosition();
        LOG.info("Last-position: {}", lastPosition);
        return lastPosition;
    }

    public String lastOrInitialPosition(Long initialPosition) {
        return lastOrInitialPosition(String.valueOf(initialPosition));
    }

    public String lastOrInitialPosition(Integer initialPosition) {
        return lastOrInitialPosition(String.valueOf(initialPosition));
    }

    public String lastOrInitialPosition(String initialPosition) {
        if (hasLastPosition()) {
            return lastPosition();
        } else {
            LOG.info("Start-position: {}", initialPosition);
            return initialPosition;
        }
    }
}