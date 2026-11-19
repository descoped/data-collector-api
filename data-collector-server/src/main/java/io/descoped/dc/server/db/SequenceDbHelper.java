package io.descoped.dc.server.db;

import io.descoped.config.DynamicConfiguration;
import io.descoped.dc.api.ids.TimeOrderedIds;
import io.descoped.dc.api.util.CommonUtils;
import io.descoped.dc.server.recovery.PositionAndIdVersion;
import org.lmdbjava.CursorIterable;
import org.lmdbjava.Dbi;
import org.lmdbjava.Txn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class SequenceDbHelper {

    private static final Logger LOG = LoggerFactory.getLogger(SequenceDbHelper.class);

    private final LmdbEnvironment lmdbEnvironment;
    private final Dbi<ByteBuffer> dbi;

    public SequenceDbHelper(LmdbEnvironment lmdbEnvironment, Dbi<ByteBuffer> dbi) {
        this.lmdbEnvironment = lmdbEnvironment;
        this.dbi = dbi;
    }

    public static Path getSequenceDatabaseLocation(DynamicConfiguration configuration) {
        String location = configuration.find("data.collector.integrityCheck.database.location").orElse(null);
        return ((Supplier<Path>) () -> {
            if (location == null || location.isEmpty()) {
                // dev directory
                return CommonUtils.currentPath().resolve("lmdb");
            } else if (location.startsWith("./")) {
                // relative path if starts with dot slash
                return CommonUtils.currentPath().resolve(location.substring(2)).resolve("lmdb");
            } else if (location.startsWith(".\\")) {
                // relative path if starts with dot slash
                return CommonUtils.currentPath().resolve(location.substring(2)).resolve("lmdb");
            } else if (location.startsWith(".//")) {
                // relative path if starts with dot slash
                return CommonUtils.currentPath().resolve(location.substring(3)).resolve("lmdb");
            } else if (location.startsWith(".")) {
                // relative path if starts with dot
                return CommonUtils.currentPath().resolve(location.substring(1)).resolve("lmdb");
            } else {
                // absolute path
                return Paths.get(location);
            }
        }).get();
    }

    public PositionAndIdVersion findFirstPosition() {
        final PositionAndIdVersion positionAndIdVersion = new PositionAndIdVersion();
        try (Txn<ByteBuffer> txn = lmdbEnvironment.env().txnRead()) {
            Iterator<CursorIterable.KeyVal<ByteBuffer>> it = dbi.iterate(txn).iterator();
            if (it.hasNext()) {
                CursorIterable.KeyVal<ByteBuffer> next = it.next();
                SequenceKey sequenceKey = SequenceKey.fromByteBuffer(next.key());
                positionAndIdVersion.compareAndSet(sequenceKey.id(), sequenceKey.position());
            }
        }
        return positionAndIdVersion;
    }

    public PositionAndIdVersion findLastPosition() {
        final PositionAndIdVersion positionAndIdVersion = new PositionAndIdVersion();
        handlePositionDuplicates(event -> positionAndIdVersion.compareAndSet(event.idSet().iterator().next(), event.sequenceKey().position()));
        return positionAndIdVersion;
    }

    void handlePositionDuplicates(Predicate<DuplicateEvent> visit) {
        AtomicBoolean canceled = new AtomicBoolean(false);
        AtomicReference<SequenceKey> prevSequenceKey = new AtomicReference<>();
        Map<String, SortedSet<UUID>> duplicatePositionAndIdSet = new LinkedHashMap<>();
        try (Txn<ByteBuffer> txn = lmdbEnvironment.env().txnRead()) {
            Iterator<CursorIterable.KeyVal<ByteBuffer>> it = dbi.iterate(txn).iterator();
            while (!canceled.get() && it.hasNext()) {
                CursorIterable.KeyVal<ByteBuffer> next = it.next();
                ByteBuffer keyBuffer = next.key();
                SequenceKey sequenceKey = SequenceKey.fromByteBuffer(keyBuffer);

                // mark first position
                if (prevSequenceKey.get() == null) {
                    prevSequenceKey.set(sequenceKey);
                    continue;
                }

                // check if we got a duplicate
                if (prevSequenceKey.get().position().equals(sequenceKey.position())) {
                    // make counters for position and increment duplicateCount
                    duplicatePositionAndIdSet.computeIfAbsent(prevSequenceKey.get().position(), duplicateIdSet -> new TreeSet<>(TimeOrderedIds.ORDER)).add(prevSequenceKey.get().id());
                    duplicatePositionAndIdSet.get(sequenceKey.position()).add(sequenceKey.id());
                }

                // fire event on duplicates when prev is not equal to curr
                if (duplicatePositionAndIdSet.size() > 0 && !prevSequenceKey.get().position().equals(sequenceKey.position())) {
                    SortedSet<UUID> idSet = duplicatePositionAndIdSet.get(prevSequenceKey.get().position());
                    try {
                        if (!visit.test(new DuplicateEvent(prevSequenceKey.get(), idSet, it.hasNext()))) {
                            canceled.set(true);
                        }
                    } catch (Exception e) {
                        LOG.error("Duplicate iteration error: {}", CommonUtils.captureStackTrace(e));
                        canceled.set(true);
                    }
                    duplicatePositionAndIdSet.clear();
                }

                // move marker to next
                prevSequenceKey.set(sequenceKey);
            }
        }
    }

    static class DuplicateEvent {
        private final SequenceKey sequenceKey;
        private final SortedSet<UUID> idSet;
        private final boolean hasNext;

        DuplicateEvent(SequenceKey sequenceKey, SortedSet<UUID> idSet, boolean hasNext) {
            this.sequenceKey = sequenceKey;
            this.idSet = idSet;
            this.hasNext = hasNext;
        }

        SequenceKey sequenceKey() {
            return sequenceKey;
        }

        SortedSet<UUID> idSet() {
            return idSet;
        }

        boolean hasNext() {
            return hasNext;
        }
    }

}
