package no.ssb.dc.content.provider.discarding;

import no.ssb.dc.api.content.ContentStateKey;
import no.ssb.dc.api.content.ContentStore;
import no.ssb.dc.api.content.ContentStream;
import no.ssb.dc.api.content.ContentStreamBuffer;
import no.ssb.dc.api.content.ContentStreamProducer;
import no.ssb.dc.api.content.HealthContentStreamMonitor;
import no.ssb.dc.api.content.HttpRequestInfo;
import no.ssb.dc.api.content.MetadataContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class DiscardingContentStore implements ContentStore {

    private final static Logger LOG = LoggerFactory.getLogger(DiscardingContentStore.class);

    final AtomicReference<String> lastPositionRef = new AtomicReference<>();
    final Map<ContentStateKey, Set<String>> contentBuffers = new ConcurrentHashMap<>();
    final AtomicBoolean closed = new AtomicBoolean(false);

    @Override
    public void lock(String topic) {
    }

    @Override
    public void unlock(String topic) {
    }

    @Override
    public ContentStream contentStream() {
        return new DiscardingContentStream();
    }

    @Override
    public String lastPosition(String topic) {
        return lastPositionRef.get();
    }

    @Override
    public Set<String> contentKeys(String topic, String position) {
        Set<String> buffers = contentBuffers.get(new ContentStateKey(topic, position));
        return (buffers == null ? new HashSet<>() : buffers);
    }

    @Override
    public void addPaginationDocument(String topic, String position, String contentKey, byte[] content, HttpRequestInfo httpRequestInfo) {
        ContentStream contentStream = new DiscardingContentStream();
        ContentStreamProducer producer = contentStream.producer(topic + "-pages");
        ContentStreamBuffer.Builder bufferBuilder = producer.builder();

        //String position = httpRequestInfo.getCorrelationIds().first().toString();
        MetadataContent manifest = getMetadataContent(topic + "-pages", position, contentKey, content, MetadataContent.ResourceType.PAGE, httpRequestInfo);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Buffer Page: {}", manifest.toJSON());
        }

        bufferBuilder.position(position);
        bufferBuilder.buffer(contentKey, content, manifest);

        producer.produce(bufferBuilder);
    }

    @Override
    public void bufferPaginationEntryDocument(String topic, String position, String contentKey, byte[] content, HttpRequestInfo httpRequestInfo) {
        MetadataContent manifest = getMetadataContent(topic, position, contentKey, content, MetadataContent.ResourceType.ENTRY, httpRequestInfo);
        if (LOG.isTraceEnabled()) {
            System.out.printf("%n");
            LOG.trace("Buffer Entry: {}", manifest.toJSON());
        }

        contentBuffers.computeIfAbsent(new ContentStateKey(topic, position), keys -> new HashSet<>()).add(contentKey);

        ContentStream contentStream = new DiscardingContentStream();
        ContentStreamProducer producer = contentStream.producer(topic);
        ContentStreamBuffer.Builder bufferBuilder = producer.builder();
        bufferBuilder.position(position).buffer(contentKey, content, manifest);

        producer.produce(bufferBuilder);
    }

    @Override
    public void bufferDocument(String topic, String position, String contentKey, byte[] content, HttpRequestInfo httpRequestInfo) {
        MetadataContent manifest = getMetadataContent(topic, position, contentKey, content, MetadataContent.ResourceType.DOCUMENT, httpRequestInfo);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Buffer Document: {}", manifest.toJSON());
        }

        contentBuffers.computeIfAbsent(new ContentStateKey(topic, position), keys -> new HashSet<>()).add(contentKey);

        ContentStream contentStream = new DiscardingContentStream();
        ContentStreamProducer producer = contentStream.producer(topic);
        ContentStreamBuffer.Builder bufferBuilder = producer.builder();
        bufferBuilder.position(position).buffer(contentKey, content, manifest);

        producer.produce(bufferBuilder);
    }

    @Override
    public void publish(String topic, String... position) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Publish Positions: {}", List.of(position));
        }
        for (String pos : position) {
            if (LOG.isTraceEnabled()) {
            }
            lastPositionRef.set(pos);
            contentBuffers.remove(new ContentStateKey(topic, pos));
        }
    }

    @Override
    public HealthContentStreamMonitor monitor() {
        return new HealthContentStreamMonitor(() -> true, () -> 0, () -> 0);
    }

    @Override
    public boolean isClosed() {
        return closed.get();
    }

    @Override
    public void closeTopic(String topic) {
    }

    @Override
    public void close() throws Exception {
        closed.set(true);
    }

    MetadataContent getMetadataContent(String topic, String position, String contentKey, byte[] content, MetadataContent.ResourceType resourceType, HttpRequestInfo httpRequestInfo) {
        return new MetadataContent.Builder()
                .resourceType(resourceType)
                .correlationId(httpRequestInfo.getCorrelationIds())
                .url(httpRequestInfo.getUrl())
                .statusCode(httpRequestInfo.getStatusCode())
                .topic(topic)
                .position(position)
                .contentKey(contentKey)
                .contentType(httpRequestInfo.getResponseHeaders().firstValue("content-type").orElseGet(() -> "application/octet-stream"))
                .contentLength(content.length)
                .requestDurationNanoTime(httpRequestInfo.getRequestDurationNanoSeconds())
                .requestHeaders(httpRequestInfo.getRequestHeaders())
                .responseHeaders(httpRequestInfo.getResponseHeaders())
                .build();
    }
}
