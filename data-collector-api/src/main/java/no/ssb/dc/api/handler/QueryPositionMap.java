package no.ssb.dc.api.handler;

import no.ssb.dc.api.Position;

import java.util.Map;

public interface QueryPositionMap {
    Map<Position<?>, ?> map();
}
