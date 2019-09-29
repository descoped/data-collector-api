package no.ssb.dc.api.delegate;

import no.ssb.dc.api.Position;

public interface QueryItemListItem {
    Tuple<Position<?>, String> item();
}
