package io.descoped.dc.api.ids;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TimeOrderedIdsTest {

    @Test
    void thatIdsAreUuidV7WithTheCurrentTime() {
        long before = System.currentTimeMillis();
        UUID id = TimeOrderedIds.generate();
        long after = System.currentTimeMillis();
        assertEquals(7, id.version());
        assertEquals(2, id.variant());
        long timestamp = TimeOrderedIds.timestamp(id);
        assertTrue(before <= timestamp && timestamp <= after, () -> timestamp + " not in [" + before + ", " + after + "]");
    }

    @Test
    void thatIdsIncreaseWithinAMillisecond() {
        List<UUID> ids = new ArrayList<>();
        for (int i = 0; i < 10_000; i++) {
            ids.add(TimeOrderedIds.generate());
        }
        for (int i = 1; i < ids.size(); i++) {
            assertTrue(TimeOrderedIds.ORDER.compare(ids.get(i - 1), ids.get(i)) < 0, "ids out of order at " + i);
        }
    }

    @Test
    void thatOrderIsUnsigned() {
        // The high bit set: a negative long, which UUID.compareTo would sort first.
        UUID low = new UUID(0x0000_0000_0000_7000L, 0x8000_0000_0000_0000L);
        UUID high = new UUID(0x8000_0000_0000_7000L, 0x8000_0000_0000_0000L);
        assertTrue(TimeOrderedIds.ORDER.compare(low, high) < 0);
        assertTrue(low.compareTo(high) > 0);
    }

    @Test
    void thatTimestampRefusesOtherUuids() {
        assertThrows(IllegalArgumentException.class, () -> TimeOrderedIds.timestamp(UUID.randomUUID()));
    }
}
