package no.ssb.dc.api.delegate;

import no.ssb.dc.api.Position;

public interface QueryPosition {
    Tuple<Position<?>, String> item();
}
