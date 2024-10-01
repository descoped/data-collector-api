package io.descoped.dc.api;

import io.descoped.dc.api.context.ExecutionContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class PageContext {

    private final List<String> expectedPositions;
    private final List<String> nextPositionVariableNames;
    private final Map<String, String> nextPositionMap; // variableName, positionValue
    private final List<Entry> parallelFutures = new ArrayList<>();
    private final CompletionInfo completionInfo = new CompletionInfo();
    private final AtomicBoolean endOfStream = new AtomicBoolean(false);
    private final AtomicReference<Throwable> failureCause = new AtomicReference<>();

    private PageContext(List<String> expectedPositions, List<String> nextPositionVariableNames, Map<String, String> nextPositionMap) {
        this.expectedPositions = expectedPositions;
        this.nextPositionVariableNames = nextPositionVariableNames;
        this.nextPositionMap = nextPositionMap;
    }

    public static PageContext createEndOfStream() {
        PageContext pageContext = new PageContext(new LinkedList<>(), new LinkedList<>(), new LinkedHashMap<>());
        pageContext.endOfStream.set(true);
        return pageContext;
    }

    public List<String> expectedPositions() {
        return expectedPositions;
    }

    public List<String> nextPositionVariableNames() {
        return nextPositionVariableNames;
    }

    public Map<String, String> nextPositionMap() {
        return nextPositionMap;
    }

    public void addFuture(CompletableFuture<ExecutionContext> future) {
        parallelFutures.add(new Entry(future));
    }

    public List<Entry> parallelPageEntries() {
        return parallelFutures;
    }

    public List<CompletableFuture<ExecutionContext>> parallelFutures() {
        return parallelFutures.stream().map(Entry::future).collect(Collectors.toList());
    }

    public CompletionInfo completionInfo() {
        return completionInfo;
    }

    public void incrementQueueCount() {
        completionInfo.queueCount.incrementAndGet();
    }

    public void incrementCompletionCount() {
        completionInfo.completedCount.incrementAndGet();
    }

    public boolean isEndOfStream() {
        return endOfStream.get();
    }

    public void setEndOfStream(boolean endOfStream) {
        this.endOfStream.set(endOfStream);
    }

    public AtomicReference<Throwable> failureCause() {
        return failureCause;
    }

    public boolean isFailure() {
        return failureCause.get() != null;
    }

    public PageContext failure(Throwable cause) {
        failureCause.set(cause);
        return this;
    }

    public Throwable getFailureCause() {
        return failureCause.get();
    }

    public static class Entry {
        private CompletableFuture<ExecutionContext> future;

        public Entry(CompletableFuture<ExecutionContext> future) {
            this.future = future;
        }

        public CompletableFuture<ExecutionContext> future() {
            return future;
        }
    }

    public static class CompletionInfo {
        final AtomicLong queueCount = new AtomicLong(0);
        final AtomicLong completedCount = new AtomicLong(0);

        public AtomicLong queueCount() {
            return queueCount;
        }

        public AtomicLong completedCount() {
            return completedCount;
        }
    }

    public static class Builder {
        private List<String> expectedPositions;
        private Map<String, String> nextPositionMap = new LinkedHashMap<>();
        private List<String> nextPositionVariableNames;

        public Builder expectedPositions(List<String> expectedPositions) {
            this.expectedPositions = expectedPositions;
            return this;
        }

        public Builder addNextPositionVariableNames(List<String> nextPositionVariableNames) {
            this.nextPositionVariableNames = nextPositionVariableNames;
            return this;
        }

        public List<String> nextPositionVariableNames() {
            return nextPositionVariableNames;
        }

        public Builder addNextPosition(String variableName, String position) {
            nextPositionMap.put(variableName, position);
            return this;
        }

        public PageContext build() {
            return new PageContext(expectedPositions, nextPositionVariableNames, nextPositionMap);
        }
    }
}
