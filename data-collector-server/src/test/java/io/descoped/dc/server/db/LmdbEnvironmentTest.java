package io.descoped.dc.server.db;

import io.descoped.dc.api.ids.TimeOrderedIds;
import io.descoped.dc.api.util.CommonUtils;
import io.descoped.dc.server.integrity.IntegrityCheckIndex;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static io.descoped.dc.server.db.LmdbEnvironment.removePath;

/**
 * Test requires vm arg: --add-opens java.base/java.nio=lmdbjava --add-exports=java.base/sun.nio.ch=lmdbjava
 */
public class LmdbEnvironmentTest {

    private static final Logger LOG = LoggerFactory.getLogger(LmdbEnvironmentTest.class);

    @Disabled
    @Test
    public void testSequence() throws IOException {
        Path dbPath = CommonUtils.currentPath().resolve("target").resolve("lmdb");
        removePath(dbPath);

        try (LmdbEnvironment environment = new LmdbEnvironment(null, dbPath, "test-stream")) {
            try (IntegrityCheckIndex index = new IntegrityCheckIndex(environment)) {

                for (int n = 1; n < 100; n++) {
                    {
                        UUID id = TimeOrderedIds.generate();
                        index.writeSequence(id, String.valueOf(n));
                    }

                    if (n == 11 || n == 88) {
                        UUID id = TimeOrderedIds.generate();
                        index.writeSequence(id, String.valueOf(n));
                    }

                    if (n == 88) {
                        UUID id = TimeOrderedIds.generate();
                        index.writeSequence(id, String.valueOf(n));
                    }
                }
                index.commitQueue();

                AtomicReference<SequenceKey> prevSequenceKey = new AtomicReference<>();
                Map<String, Set<SequenceKey>> duplicateMap = new LinkedHashMap<>();
                index.readSequence((sequenceKey, hasNext) -> {
                    if (prevSequenceKey.get() == null) {
                        prevSequenceKey.set(sequenceKey);
                        return;
                    }
                    if (prevSequenceKey.get().position().equals(sequenceKey.position())) {
                        duplicateMap.computeIfAbsent(prevSequenceKey.get().position(), duplicateList -> new TreeSet<>()).add(prevSequenceKey.get());
                        duplicateMap.get(sequenceKey.position()).add(sequenceKey);
                    }
                    prevSequenceKey.set(sequenceKey);
                });

                duplicateMap.forEach((sequenceKey, duplicateList) ->
                        LOG.trace("{}/{}", sequenceKey, duplicateList.stream().map(key ->
                                Long.toString(TimeOrderedIds.timestamp(key.id()) + key.id().getLeastSignificantBits())).collect(Collectors.joining(",")))
                );

                index.readSequence((sequenceKey, hasNext) -> {
                    LOG.trace("{}/{}", sequenceKey.position(), sequenceKey.id());
                });
            }
        }
    }

}
