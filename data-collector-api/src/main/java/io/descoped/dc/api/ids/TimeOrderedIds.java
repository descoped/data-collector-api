package io.descoped.dc.api.ids;

import java.time.Instant;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Time-ordered ids: UUIDv7 (RFC 9562), the ids rawdata-client 3 gives messages, laid out the same way.
 * <pre>
 *  48 bits  unix_ts_ms   milliseconds since 1970-01-01T00:00Z
 *   4 bits  version      7
 *  12 bits  rand_a       sub-millisecond fraction (RFC 9562 §6.2, method 3)
 *   2 bits  variant      0b10
 *  62 bits  rand_b       random
 * </pre>
 * Ids sort by time in the order of {@link #ORDER}, which compares the 16 bytes as unsigned numbers.
 * {@link UUID#compareTo} compares signed longs and must not be used to order ids.
 */
public final class TimeOrderedIds {

    /**
     * The order of ids: the 128 bits compared as an unsigned number, as rawdata-client and PostgreSQL order them.
     */
    public static final Comparator<UUID> ORDER = (a, b) -> {
        int most = Long.compareUnsigned(a.getMostSignificantBits(), b.getMostSignificantBits());
        return most == 0 ? Long.compareUnsigned(a.getLeastSignificantBits(), b.getLeastSignificantBits()) : most;
    };

    private static final long VERSION_7 = 0x7000L;
    private static final long VARIANT = 0x8000_0000_0000_0000L;
    private static final long RAND_A_MASK = 0x0FFFL;
    private static final long RAND_B_MASK = 0x3FFF_FFFF_FFFF_FFFFL;

    private static final AtomicReference<UUID> LAST = new AtomicReference<>();

    private TimeOrderedIds() {
    }

    /**
     * Generates an id greater than every id generated before it in this JVM, also when several are made in one
     * millisecond or the clock moves back.
     *
     * @return a new UUIDv7
     */
    public static UUID generate() {
        return LAST.updateAndGet(previous -> {
            UUID generated = random();
            return previous == null || ORDER.compare(generated, previous) > 0 ? generated : increment(previous);
        });
    }

    /**
     * Returns the time of an id.
     *
     * @param id a UUIDv7
     * @return the milliseconds since 1970-01-01T00:00Z
     * @throws IllegalArgumentException if the id is not a UUIDv7
     */
    public static long timestamp(UUID id) {
        Objects.requireNonNull(id, "id");
        if (id.version() != 7 || id.variant() != 2) {
            throw new IllegalArgumentException("Not a UUIDv7: " + id);
        }
        return id.getMostSignificantBits() >>> 16;
    }

    private static UUID random() {
        Instant now = Instant.now();
        long millis = now.toEpochMilli();
        long fraction = ((now.getNano() % 1_000_000L) * 4096L) / 1_000_000L;
        long randB = ThreadLocalRandom.current().nextLong() & RAND_B_MASK;
        return new UUID((millis << 16) | VERSION_7 | fraction, VARIANT | randB);
    }

    private static UUID increment(UUID id) {
        long timestamp = id.getMostSignificantBits() >>> 16;
        long randA = id.getMostSignificantBits() & RAND_A_MASK;
        long randB = id.getLeastSignificantBits() & RAND_B_MASK;
        if (randB < RAND_B_MASK) {
            randB++;
        } else {
            randB = 0;
            if (randA < RAND_A_MASK) {
                randA++;
            } else {
                randA = 0;
                timestamp++;
            }
        }
        return new UUID((timestamp << 16) | VERSION_7 | randA, VARIANT | randB);
    }
}
