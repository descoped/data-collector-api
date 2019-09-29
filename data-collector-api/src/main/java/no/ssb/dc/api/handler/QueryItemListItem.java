package no.ssb.dc.api.handler;

import no.ssb.dc.api.Position;

public interface QueryItemListItem {
    Tuple<Position<?>, String> item();
}
