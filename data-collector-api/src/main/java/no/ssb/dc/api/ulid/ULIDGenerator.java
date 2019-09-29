package no.ssb.dc.api.ulid;

import de.huxhorn.sulky.ulid.ULID;

import java.util.UUID;

public class ULIDGenerator {

    public static ULID.Value nextMonotonicUlid(ULIDStateHolder ulidStateHolder) {
        /*
         * Will spin until time ticks if next value overflows.
         * Although theoretically possible, it is extremely unlikely that the loop will ever spin
         */
        ULID.Value value;
        ULID.Value previousUlid = ulidStateHolder.prevUlid.get();
        do {
            long timestamp = System.currentTimeMillis();
            if (previousUlid.timestamp() > timestamp) {
                throw new IllegalStateException("Previous timestamp is in the future");
            } else if (previousUlid.timestamp() != timestamp) {
                // start at lsb 1, to avoid inclusive/exclusive semantics when searching
                return new ULID.Value((timestamp << 16) & 0xFFFFFFFFFFFF0000L, 1L);
            }
            // previousUlid.timestamp() == timestamp
            value = ulidStateHolder.ulid.nextStrictlyMonotonicValue(previousUlid, timestamp).orElse(null);
        } while (value == null);
        return value;
    }

    public static UUID toUUID(ULID.Value ulid) {
        return new UUID(ulid.getMostSignificantBits(), ulid.getLeastSignificantBits());
    }

}
