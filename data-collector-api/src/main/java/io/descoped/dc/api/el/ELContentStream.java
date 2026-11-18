package io.descoped.dc.api.el;

import io.descoped.dc.api.content.EvaluateLastContentStreamPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ELContentStream {
    private static final Logger LOG = LoggerFactory.getLogger(ELContentStream.class);
    private final EvaluateLastContentStreamPosition evaluateLastContentStreamPosition;

    ELContentStream(EvaluateLastContentStreamPosition evaluateLastContentStreamPosition) {
        this.evaluateLastContentStreamPosition = evaluateLastContentStreamPosition;
    }

    /*
     * Topic page position
     */

    public boolean hasLastPagePosition() {
        LOG.debug("Check hasLastPagePosition");
        return evaluateLastContentStreamPosition.getLastPagePosition() != null;
    }

    public String lastPagePosition() {
        LOG.debug("Get lastPagePosition");
        String lastPagePosition = evaluateLastContentStreamPosition.getLastPagePosition();
        if (lastPagePosition != null) LOG.info("Last-PagePosition: {}", lastPagePosition);
        return lastPagePosition;
    }

    public String lastOrInitialPagePosition(Long initialPagePosition) {
        LOG.debug("Get lastOrInitialPagePosition (Long): {}", initialPagePosition);
        return lastOrInitialPagePosition(String.valueOf(initialPagePosition));
    }

    public String lastOrInitialPagePosition(Integer initialPagePosition) {
        LOG.debug("Get lastOrInitialPagePosition (Integer): {}", initialPagePosition);
        return lastOrInitialPagePosition(String.valueOf(initialPagePosition));
    }

    public String lastOrInitialPagePosition(String initialPagePosition) {
        LOG.debug("Get lastOrInitialPagePosition (String): {}", initialPagePosition);
        String lastPagePosition = lastPagePosition();
        if (lastPagePosition != null) {
            return lastPagePosition;
        } else {
            LOG.info("Start-PagePosition: {}", initialPagePosition);
            return initialPagePosition;
        }
    }

    /*
     * Topic position
     */

    public boolean hasLastPosition() {
        LOG.debug("Check hasLastPosition");
        return evaluateLastContentStreamPosition.getLastPosition() != null;
    }

    public String lastPosition() {
        LOG.debug("Get lastPosition");
        String lastPosition = evaluateLastContentStreamPosition.getLastPosition();
        if (lastPosition != null) LOG.info("Last-position: {}", lastPosition);
        return lastPosition;
    }

    public String lastOrInitialPosition(Long initialPosition) {
        LOG.debug("Get lastOrInitialPosition (Long): {}", initialPosition);
        return lastOrInitialPosition(String.valueOf(initialPosition));
    }

    public String lastOrInitialPosition(Integer initialPosition) {
        LOG.debug("Get lastOrInitialPosition (Integer): {}", initialPosition);
        return lastOrInitialPosition(String.valueOf(initialPosition));
    }

    public String lastOrInitialPosition(String initialPosition) {
        LOG.debug("Get lastOrInitialPosition (String): {}", initialPosition);
        String lastPosition = lastPosition();
        if (lastPosition != null) {
            return lastPosition;
        } else {
            LOG.info("Start-position: {}", initialPosition);
            return initialPosition;
        }
    }
}
