package io.descoped.dc.api;

import io.descoped.dc.api.context.ExecutionContext;
import io.descoped.dc.api.ulid.ULIDGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CorrelationIds {

    final Deque<UUID> correlationIds = new ConcurrentLinkedDeque<>();
    final Lock lock = new ReentrantLock();

    private CorrelationIds() {
    }

    public static CorrelationIds of(ExecutionContext context) {
        CorrelationIds cid = context.state(CorrelationIds.class);
        if (cid == null) {
            cid = new CorrelationIds();
            context.state(CorrelationIds.class, cid);
        }
        return cid;
    }

    public static CorrelationIds create(ExecutionContext context) {
        CorrelationIds cid = new CorrelationIds();
        context.state(CorrelationIds.class, cid);
        return cid;
    }

    public UUID add() {
        UUID uuid = ULIDGenerator.toUUID(ULIDGenerator.generate());
        correlationIds.add(uuid);
        return uuid;
    }

    public void tail(CorrelationIds correlationIds) {
        this.correlationIds.add(correlationIds.last());
    }


    public UUID first() {
        return correlationIds.peekFirst();
    }

    public UUID last() {
        return correlationIds.peekLast();
    }

    public List<UUID> get() {
        try {
            if (lock.tryLock(1, TimeUnit.SECONDS)) {
                try {
                    return new ArrayList<>(correlationIds);
                } finally {
                    lock.unlock();
                }
            }
            return Collections.emptyList();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public ExecutionContext context() {
        return ExecutionContext.empty().state(CorrelationIds.class, this);
    }
}
