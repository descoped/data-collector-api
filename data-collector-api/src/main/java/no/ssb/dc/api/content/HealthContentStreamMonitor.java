package no.ssb.dc.api.content;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.ssb.dc.api.health.HealthResourceUtils;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class HealthContentStreamMonitor {

    final Supplier<Boolean> contentStreamClosedCallback;

    final AtomicLong lastSeenTimestampRef = new AtomicLong(0);

    final AtomicLong paginationDocumentCountRef = new AtomicLong(0);
    final AtomicLong paginationDocumentSizeRef = new AtomicLong(0);
    final AtomicLong lastPaginationDocumentWriteDurationRef = new AtomicLong(0);

    final AtomicLong entryBufferCountRef = new AtomicLong(0);
    final AtomicLong entryBufferSizeRef = new AtomicLong(0);
    final AtomicLong lastEntryBufferWriteDurationRef = new AtomicLong(0);

    final AtomicLong documentBufferCountRef = new AtomicLong(0);
    final AtomicLong documentBufferSizeRef = new AtomicLong(0);
    final AtomicLong lastDocumentBufferWriteDurationRef = new AtomicLong(0);

    final AtomicLong publishedBufferCountRef = new AtomicLong(0);
    final AtomicLong lastPublishedBufferCountRef = new AtomicLong(0);
    final AtomicLong publishedPositionCountRef = new AtomicLong(0);
    final AtomicLong lastPublishWriteDurationRef = new AtomicLong(0);

    public HealthContentStreamMonitor(Supplier<Boolean> contentStreamClosedCallback) {
        this.contentStreamClosedCallback = contentStreamClosedCallback;
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
    }

    public void incrementEntryBufferCount() {
        entryBufferCountRef.incrementAndGet();
    }

    public void addEntryBufferSize(int documentSize) {
        entryBufferSizeRef.addAndGet(documentSize);
    }

    public void updateLastEntryBufferWriteDuration(long durationInMillis) {
        lastEntryBufferWriteDurationRef.set(durationInMillis);
    }

    public void incrementDocumentBufferCount() {
        documentBufferCountRef.incrementAndGet();
    }

    public void addDocumentBufferSize(int documentSize) {
        documentBufferSizeRef.addAndGet(documentSize);
    }

    public void updateLastDocumentBufferWriteDuration(long durationInMillis) {
        lastDocumentBufferWriteDurationRef.set(durationInMillis);
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
        lastPublishWriteDurationRef.set(durationInMillis);
    }

    public ContentStreamInfo build() {
        return new ContentStreamInfo(
                lastSeenTimestampRef.get() == 0L ? null : Instant.ofEpochMilli(lastSeenTimestampRef.get()).toString(),
                paginationDocumentCountRef.get(),
                Math.round(HealthResourceUtils.divide(paginationDocumentSizeRef.get(), paginationDocumentCountRef.get())),
                lastPaginationDocumentWriteDurationRef.get(),
                entryBufferCountRef.get(),
                Math.round(HealthResourceUtils.divide(entryBufferSizeRef.get(), entryBufferCountRef.get())),
                lastEntryBufferWriteDurationRef.get(),
                documentBufferCountRef.get(),
                Math.round(HealthResourceUtils.divide(documentBufferSizeRef.get(), documentBufferCountRef.get())),
                lastDocumentBufferWriteDurationRef.get(),
                publishedBufferCountRef.get(),
                lastPublishedBufferCountRef.get(),
                publishedPositionCountRef.get(),
                lastPublishWriteDurationRef.get()
        );
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings("WeakerAccess")
    public static class ContentStreamInfo {
        @JsonProperty("last-seen") public final String lastSeenTimestamp;

        @JsonProperty("page-document-count") public final Long paginationDocumentCount;
        @JsonProperty("avg-page-document-size-bytes") public final Integer averagePaginationDocumentSize;
        @JsonProperty("last-page-document-write-duration-millis") public final Long lastPaginationDocumentWriteDuration;

        @JsonProperty("page-entry-count") public final Long paginationEntryBufferCount;
        @JsonProperty("avg-page-entry-size-bytes") public final Integer averagePaginationEntryBufferSize;
        @JsonProperty("last-page-entry-write-duration-millis") public final Long lastEntryBufferWriteDuration;

        @JsonProperty("entry-document-count") public final Long documentBufferCount;
        @JsonProperty("avg-entry-document-size-bytes") public final Integer averageDocumentBufferSize;
        @JsonProperty("last-entry-document-write-duration-millis") public final Long lastDocumentBufferWriteDuration;

        @JsonProperty("published-buffer-count") public final Long publishedBufferCount;
        @JsonProperty("last-published-buffer-count") public final Long lastPublishedBufferCount;
        @JsonProperty("published-position-count") public final Long publishedPositionCount;
        @JsonProperty("last-published-position-write-duration-millis") public final Long lastPublishWriteDuration;

        ContentStreamInfo(String lastSeenTimestamp,
                          Long paginationDocumentCount,
                          Integer averagePaginationDocumentSize,
                          Long lastPaginationDocumentWriteDuration,
                          Long paginationEntryBufferCount,
                          Integer averagePaginationEntryBufferSize,
                          Long lastEntryBufferWriteDuration,
                          Long documentBufferCount,
                          Integer averageDocumentBufferSize,
                          Long lastDocumentBufferWriteDuration,
                          Long publishedBufferCount,
                          Long lastPublishedBufferCount,
                          Long publishedPositionCount,
                          Long lastPublishWriteDuration
        ) {
            this.lastSeenTimestamp = lastSeenTimestamp;
            this.paginationDocumentCount = paginationDocumentCount;
            this.averagePaginationDocumentSize = averagePaginationDocumentSize;
            this.lastPaginationDocumentWriteDuration = lastPaginationDocumentWriteDuration;
            this.paginationEntryBufferCount = paginationEntryBufferCount;
            this.averagePaginationEntryBufferSize = averagePaginationEntryBufferSize;
            this.lastEntryBufferWriteDuration = lastEntryBufferWriteDuration;
            this.documentBufferCount = documentBufferCount;
            this.averageDocumentBufferSize = averageDocumentBufferSize;
            this.lastDocumentBufferWriteDuration = lastDocumentBufferWriteDuration;
            this.publishedBufferCount = publishedBufferCount;
            this.lastPublishedBufferCount = lastPublishedBufferCount;
            this.publishedPositionCount = publishedPositionCount;
            this.lastPublishWriteDuration = lastPublishWriteDuration;
        }
    }
}
