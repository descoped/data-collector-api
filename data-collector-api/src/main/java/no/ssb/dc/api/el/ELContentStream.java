package no.ssb.dc.api.el;

import no.ssb.dc.api.content.EvaluateLastContentStreamPosition;

public class ELContentStream {
    private final EvaluateLastContentStreamPosition evaluateLastContentStreamPosition;

    public ELContentStream(EvaluateLastContentStreamPosition evaluateLastContentStreamPosition) {
        this.evaluateLastContentStreamPosition = evaluateLastContentStreamPosition;
    }

    public String lastPosition() {
        return evaluateLastContentStreamPosition.getLastPosition();
    }
}
