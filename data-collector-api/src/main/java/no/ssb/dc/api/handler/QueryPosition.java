package no.ssb.dc.api.handler;

import no.ssb.dc.api.Position;

public interface QueryPosition {
    Tuple<Position<?>, String> item();
}
