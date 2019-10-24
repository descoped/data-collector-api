package no.ssb.dc.api.content;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.ssb.dc.api.health.HealthResourceUtils;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class HealthContentStreamMonitor {

    final Supplier<Boolean> contentStreamClosedCallback;

    final AtomicLong lastSeenTimestamp = new AtomicLong(0);

    final AtomicLong paginationDocumentCount = new AtomicLong(0);
    final AtomicLong paginationDocumentSize = new AtomicLong(0);

    final AtomicLong entryBufferCount = new AtomicLong(0);
    final AtomicLong entryBufferSize = new AtomicLong(0);

    final AtomicLong documentBufferCount = new AtomicLong(0);
    final AtomicLong documentBufferSize = new AtomicLong(0);

    final AtomicLong publishedBufferCount = new AtomicLong(0);
    final AtomicLong publishedPositionCount = new AtomicLong(0);

    public HealthContentStreamMonitor(Supplier<Boolean> contentStreamClosedCallback) {
        this.contentStreamClosedCallback = contentStreamClosedCallback;
    }

    public boolean isUp() {
        return !contentStreamClosedCallback.get();
    }

    public void updateLastSeen() {
        lastSeenTimestamp.set(Instant.now().toEpochMilli());
    }

    public void incrementPaginationDocumentCount() {
        paginationDocumentCount.incrementAndGet();
    }

    public void addPaginationDocumentSize(int documentSize) {
        paginationDocumentSize.addAndGet(documentSize);
    }

    public void incrementEntryBufferCount() {
        entryBufferCount.incrementAndGet();
    }

    public void addEntryBufferSize(int documentSize) {
        entryBufferSize.addAndGet(documentSize);
    }

    public void incrementDocumentBufferCount() {
        documentBufferCount.incrementAndGet();
    }

    public void addDocumentBufferSize(int documentSize) {
        documentBufferSize.addAndGet(documentSize);
    }

    public void addPublishedBufferCount(int bufferCount) {
        publishedBufferCount.addAndGet(bufferCount);
    }

    public void addPublishedPositionCount(int bufferCount) {
        publishedPositionCount.addAndGet(bufferCount);
    }

    public ContentStreamInfo build() {
        return new ContentStreamInfo(
                lastSeenTimestamp.get() == 0L ? null : Instant.ofEpochMilli(lastSeenTimestamp.get()).toString(),
                paginationDocumentCount.get(),
                Math.round(HealthResourceUtils.divide(paginationDocumentSize.get(), paginationDocumentCount.get())),
                entryBufferCount.get(),
                Math.round(HealthResourceUtils.divide(entryBufferSize.get(), entryBufferCount.get())),
                documentBufferCount.get(),
                Math.round(HealthResourceUtils.divide(documentBufferSize.get(), documentBufferCount.get())),
                publishedBufferCount.get(),
                publishedPositionCount.get()
        );
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @SuppressWarnings("WeakerAccess")
    public static class ContentStreamInfo {
        @JsonProperty("last-seen") public final String lastSeenTimestamp;

        @JsonProperty("page-document-count") public final Long paginationDocumentCount;
        @JsonProperty("avg-page-document-size") public final Integer averagePaginationDocumentSize;

        @JsonProperty("page-entry-count") public final Long paginationEntryBufferCount;
        @JsonProperty("avg-page-entry-size") public final Integer averagePaginationEntryBufferSize;

        @JsonProperty("entry-document-count") public final Long documentBufferCount;
        @JsonProperty("avg-entry-document-size") public final Integer averageDocumentBufferSize;

        @JsonProperty("published-buffer-count") public final Long publishedBufferCount;
        @JsonProperty("published-position-count") public final Long publishedPositionCount;

        ContentStreamInfo(String lastSeenTimestamp,
                                 Long paginationDocumentCount,
                                 Integer averagePaginationDocumentSize,
                                 Long paginationEntryBufferCount,
                                 Integer averagePaginationEntryBufferSize,
                                 Long documentBufferCount,
                                 Integer averageDocumentBufferSize,
                                 Long publishedBufferCount,
                                 Long publishedPositionCount
        ) {
            this.lastSeenTimestamp = lastSeenTimestamp;
            this.paginationDocumentCount = paginationDocumentCount;
            this.averagePaginationDocumentSize = averagePaginationDocumentSize;
            this.paginationEntryBufferCount = paginationEntryBufferCount;
            this.averagePaginationEntryBufferSize = averagePaginationEntryBufferSize;
            this.documentBufferCount = documentBufferCount;
            this.averageDocumentBufferSize = averageDocumentBufferSize;
            this.publishedBufferCount = publishedBufferCount;
            this.publishedPositionCount = publishedPositionCount;
        }
    }
}
