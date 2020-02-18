package no.ssb.dc.api;

import no.ssb.dc.api.context.ExecutionContext;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CorrelationIdsTest {

    @Test
    public void thatCorrelationIdIsBoundAndUnboundFromContext() {
        ExecutionContext context = ExecutionContext.empty();
        CorrelationIds cid = CorrelationIds.of(context);
        UUID uuid = cid.add();
        assertEquals(cid.first(), uuid);
        assertEquals(cid.last(), uuid);
    }
}
