package no.ssb.dc.api.ulid;

import de.huxhorn.sulky.ulid.ULID;

import java.util.concurrent.atomic.AtomicReference;

public class ULIDStateHolder {

    final ULID ulid = new ULID();

    final AtomicReference<ULID.Value> prevUlid = new AtomicReference<>(ulid.nextValue());

}
