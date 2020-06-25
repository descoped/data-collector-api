package no.ssb.dc.api.ulid;

import de.huxhorn.sulky.ulid.ULID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class ULIDGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(ULIDGenerator.class);

    public static ULID.Value nextMonotonicUlid(ULIDStateHolder ulidStateHolder) {
        /*
         * Will spin until time ticks if next value overflows.
         * Although theoretically possible, it is extremely unlikely that the loop will ever spin
         */
        ULID.Value value;
        ULID.Value previousUlid = ulidStateHolder.prevUlid.get();
        do {
            long timestamp = System.currentTimeMillis();
            long diff = timestamp - previousUlid.timestamp();
            if (diff < 0) {
                if (diff < -(30 * 1000)) {
                    throw new IllegalStateException(String.format("Previous timestamp is in the future. Diff %d ms", -diff));
                }
                LOG.debug("Previous timestamp is in the future, waiting for time to catch up. Diff {} ms", -diff);
                try {
                    Thread.sleep(-diff);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else if (diff > 0) {
                // start at lsb 1, to avoid inclusive/exclusive semantics when searching
                value = new ULID.Value((timestamp << 16) & 0xFFFFFFFFFFFF0000L, 1L);
                ulidStateHolder.prevUlid.set(value);
                return value;
            }
            // diff == 0
            value = ulidStateHolder.ulid.nextStrictlyMonotonicValue(previousUlid, timestamp).orElse(null);
            ulidStateHolder.prevUlid.set(value);
        } while (value == null);
        ulidStateHolder.prevUlid.set(value);
        return value;
    }

    public static UUID toUUID(ULID.Value ulid) {
        return new UUID(ulid.getMostSignificantBits(), ulid.getLeastSignificantBits());
    }

}
