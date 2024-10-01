package io.descoped.dc.api.content;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.descoped.dc.api.health.HealthResourceUtils;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class HealthContentStreamMonitor {

    final Supplier<Boolean> contentStreamClosedCallback;
    private final Supplier<Integer> activePositionCountSupplier;
    final Supplier<Integer> activeBufferCountSupplier;

    final AtomicLong lastSeenTimestampRef = new AtomicLong(0);

    final AtomicLong paginationDocumentCountRef = new AtomicLong(0);
    final AtomicLong paginationDocumentSizeRef = new AtomicLong(0);
    final AtomicLong lastPaginationDocumentWriteDurationRef = new AtomicLong(0);
    final AtomicLong aggregatedPaginationDocumentWriteDurationRef = new AtomicLong(0);

    final AtomicLong entryBufferCountRef = new AtomicLong(0);
    final AtomicLong entryBufferSizeRef = new AtomicLong(0);
    final AtomicLong lastEntryBufferWriteDurationRef = new AtomicLong(0);
    final AtomicLong aggregatedEntryBufferWriteDurationRef = new AtomicLong(0);

    final AtomicLong documentBufferCountRef = new AtomicLong(0);
    final AtomicLong documentBufferSizeRef = new AtomicLong(0);
    final AtomicLong lastDocumentBufferWriteDurationRef = new AtomicLong(0);
    final AtomicLong aggregatedDocumentBufferWriteDurationRef = new AtomicLong(0);

    final AtomicLong publishedBufferCountRef = new AtomicLong(0);
    final AtomicLong lastPublishedBufferCountRef = new AtomicLong(0);
    final AtomicLong publishedPositionCountRef = new AtomicLong(0);
    final AtomicLong publishedCountRef = new AtomicLong(0);
    final AtomicLong lastPublishWriteDurationRef = new AtomicLong(0);
    final AtomicLong aggregatedPublishWriteDurationRef = new AtomicLong(0);

    public HealthContentStreamMonitor(Supplier<Boolean> contentStreamClosedCallback, Supplier<Integer> activePositionCountSupplier, Supplier<Integer> activeBufferCountSupplier) {
        this.contentStreamClosedCallback = contentStreamClosedCallback;
        this.activePositionCountSupplier = activePositionCountSupplier;
        this.activeBufferCountSupplier = activeBufferCountSupplier;
    }

    public boolean isUp() {
        return !contentStreamClosedCallback.get();
    }

    public void updateLastSeen() {
        lastSeenTimestampRef.set(Instant.now().toEpochMilli());
    }

    public void incrementPaginationDocumentCount() {
        paginationDocumentCountRef.incrementAndGet();
    }

    public void addPaginationDocumentSize(int documentSize) {
        paginationDocumentSizeRef.addAndGet(documentSize);
    }

    public void updateLastPaginationDocumentWriteDuration(long durationInMillis) {
        lastPaginationDocumentWriteDurationRef.set(durationInMillis);
        aggregatedPaginationDocumentWriteDurationRef.addAndGet(durationInMillis);
    }

    public void incrementEntryBufferCount() {
        entryBufferCountRef.incrementAndGet();
    }

    public void addEntryBufferSize(int documentSize) {
        entryBufferSizeRef.addAndGet(documentSize);
    }

    public void updateLastEntryBufferWriteDuration(long durationInMillis) {
        lastEntryBufferWriteDurationRef.set(durationInMillis);
        aggregatedEntryBufferWriteDurationRef.addAndGet(durationInMillis);
    }

    public void incrementDocumentBufferCount() {
        documentBufferCountRef.incrementAndGet();
    }

    public void addDocumentBufferSize(int documentSize) {
        documentBufferSizeRef.addAndGet(documentSize);
    }

    public void updateLastDocumentBufferWriteDuration(long durationInMillis) {
        lastDocumentBufferWriteDurationRef.set(durationInMillis);
        aggregatedDocumentBufferWriteDurationRef.addAndGet(durationInMillis);
    }

    public void addPublishedBufferCount(int bufferCount) {
        publishedBufferCountRef.addAndGet(bufferCount);
    }

    public void updateLastPublishedBufferCount(int bufferCount) {
        lastPublishedBufferCountRef.set(bufferCount);
    }

    public void addPublishedPositionCount(int bufferCount) {
        publishedPositionCountRef.addAndGet(bufferCount);
    }

    public void updateLastPublishedPositionWriteDuration(long durationInMillis) {
        publishedCountRef.incrementAndGet();
        lastPublishWriteDurationRef.set(durationInMillis);
        aggregatedPublishWriteDurationRef.addAndGet(durationInMillis);
    }

    public ContentStreamInfo build() {
        return new ContentStreamInfo(
                lastSeenTimestampRef.get() == 0L ? null : Instant.ofEpochMilli(lastSeenTimestampRef.get()).toString(),
                paginationDocumentCountRef.get(),
                Math.round(HealthResourceUtils.divide(paginationDocumentSizeRef.get(), paginationDocumentCountRef.get())),
                lastPaginationDocumentWriteDurationRef.get(),
                Math.round(HealthResourceUtils.divide(aggregatedPaginationDocumentWriteDurationRef.get(), paginationDocumentCountRef.get())),
                entryBufferCountRef.get(),
                Math.round(HealthResourceUtils.divide(entryBufferSizeRef.get(), entryBufferCountRef.get())),
                lastEntryBufferWriteDurationRef.get(),
                Math.round(HealthResourceUtils.divide(aggregatedEntryBufferWriteDurationRef.get(), entryBufferCountRef.get())),
                documentBufferCountRef.get(),
                Math.round(HealthResourceUtils.divide(documentBufferSizeRef.get(), documentBufferCountRef.get())),
                lastDocumentBufferWriteDurationRef.get(),
                Math.round(HealthResourceUtils.divide(aggregatedDocumentBufferWriteDurationRef.get(), documentBufferCountRef.get())),
                activePositionCountSupplier.get(),
                activeBufferCountSupplier.get(),
                publishedBufferCountRef.get(),
                lastPublishedBufferCountRef.get(),
                publishedPositionCountRef.get(),
                lastPublishWriteDurationRef.get(),
                Math.round(HealthResourceUtils.divide(aggregatedPublishWriteDurationRef.get(), publishedCountRef.get())));
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings("WeakerAccess")
    public static class ContentStreamInfo {
        @JsonProperty("last-seen")
        public final String lastSeenTimestamp;

        @JsonProperty("page-document-count")
        public final Long paginationDocumentCount;
        @JsonProperty("avg-page-document-size-bytes")
        public final Integer averagePaginationDocumentSize;
        @JsonProperty("last-page-document-write-duration-millis")
        public final Long lastPaginationDocumentWriteDuration;
        @JsonProperty("avg-page-document-write-duration-millis")
        public final Integer averagePaginationDocumentWriteDuration;

        @JsonProperty("page-entry-count")
        public final Long paginationEntryBufferCount;
        @JsonProperty("avg-page-entry-size-bytes")
        public final Integer averagePaginationEntryBufferSize;
        @JsonProperty("last-page-entry-write-duration-millis")
        public final Long lastEntryBufferWriteDuration;
        @JsonProperty("avg-page-entry-write-duration-millis")
        public final Integer averageEntryBufferWriteDuration;

        @JsonProperty("entry-document-count")
        public final Long documentBufferCount;
        @JsonProperty("avg-entry-document-size-bytes")
        public final Integer averageDocumentBufferSize;
        @JsonProperty("last-entry-document-write-duration-millis")
        public final Long lastDocumentBufferWriteDuration;
        @JsonProperty("avg-entry-document-write-duration-millis")
        public final Integer averageDocumentBufferWriteDuration;

        @JsonProperty("active-position-count")
        public final Integer activePositionCount;
        @JsonProperty("active-buffer-count")
        public final Integer activeBufferCount;
        @JsonProperty("published-buffer-count")
        public final Long publishedBufferCount;
        @JsonProperty("last-published-buffer-count")
        public final Long lastPublishedBufferCount;
        @JsonProperty("published-position-count")
        public final Long publishedPositionCount;
        @JsonProperty("last-published-position-write-duration-millis")
        public final Long lastPublishWriteDuration;
        @JsonProperty("avg-published-position-write-duration-millis")
        public final Integer averagePublishWriteDuration;

        ContentStreamInfo(String lastSeenTimestamp,
                          Long paginationDocumentCount,
                          Integer averagePaginationDocumentSize,
                          Long lastPaginationDocumentWriteDuration,
                          Integer averagePaginationDocumentWriteDuration,
                          Long paginationEntryBufferCount,
                          Integer averagePaginationEntryBufferSize,
                          Long lastEntryBufferWriteDuration,
                          Integer averageEntryBufferWriteDuration,
                          Long documentBufferCount,
                          Integer averageDocumentBufferSize,
                          Long lastDocumentBufferWriteDuration,
                          Integer averageDocumentBufferWriteDuration,
                          Integer activePositionCount,
                          Integer activeBufferCount,
                          Long publishedBufferCount,
                          Long lastPublishedBufferCount,
                          Long publishedPositionCount,
                          Long lastPublishWriteDuration,
                          Integer averagePublishWriteDuration
        ) {
            this.lastSeenTimestamp = lastSeenTimestamp;
            this.activePositionCount = activePositionCount;
            this.activeBufferCount = activeBufferCount;
            this.paginationDocumentCount = paginationDocumentCount;
            this.averagePaginationDocumentSize = averagePaginationDocumentSize;
            this.lastPaginationDocumentWriteDuration = lastPaginationDocumentWriteDuration;
            this.averagePaginationDocumentWriteDuration = averagePaginationDocumentWriteDuration;
            this.paginationEntryBufferCount = paginationEntryBufferCount;
            this.averagePaginationEntryBufferSize = averagePaginationEntryBufferSize;
            this.lastEntryBufferWriteDuration = lastEntryBufferWriteDuration;
            this.averageEntryBufferWriteDuration = averageEntryBufferWriteDuration;
            this.documentBufferCount = documentBufferCount;
            this.averageDocumentBufferSize = averageDocumentBufferSize;
            this.lastDocumentBufferWriteDuration = lastDocumentBufferWriteDuration;
            this.averageDocumentBufferWriteDuration = averageDocumentBufferWriteDuration;
            this.publishedBufferCount = publishedBufferCount;
            this.lastPublishedBufferCount = lastPublishedBufferCount;
            this.publishedPositionCount = publishedPositionCount;
            this.lastPublishWriteDuration = lastPublishWriteDuration;
            this.averagePublishWriteDuration = averagePublishWriteDuration;
        }
    }
}
