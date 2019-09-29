package no.ssb.dc.content;

import no.ssb.dc.api.content.ContentStore;
import no.ssb.dc.api.content.ContentStream;
import no.ssb.dc.api.content.ContentStreamBuffer;
import no.ssb.dc.api.content.ContentStreamProducer;
import no.ssb.dc.api.content.HttpRequestInfo;
import no.ssb.dc.api.content.MetadataContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class DiscardingContentStore implements ContentStore {

    private final static Logger LOG = LoggerFactory.getLogger(DiscardingContentStore.class);

    final AtomicReference<String> lastPositionRef = new AtomicReference<>();

    @Override
    public String lastPosition(String namespace) {
        return lastPositionRef.get();
    }

    @Override
    public Set<String> contentKeys(String namespace, String position) {
        return new HashSet<>();
    }

    @Override
    public void addPaginationDocument(String namespace, String contentKey, byte[] content, HttpRequestInfo httpRequestInfo) {
        ContentStream contentStream = new DiscardingContentStream();
        ContentStreamProducer producer = contentStream.producer(namespace + "-pages");
        ContentStreamBuffer.Builder bufferBuilder = producer.builder();

        String position = httpRequestInfo.getCorrelationIds().first().toString();
        MetadataContent manifest = getMetadataContent(namespace + "-pages", position, contentKey, content, MetadataContent.ResourceType.PAGE, httpRequestInfo);
        if (LOG.isTraceEnabled()) {
            LOG.trace("PAGE: {}", manifest.toJSON());
        }

        bufferBuilder.position(position);
        bufferBuilder.buffer(contentKey, content, manifest);

        producer.produce(bufferBuilder);
    }

    @Override
    public void bufferPaginationEntryDocument(String namespace, String position, String contentKey, byte[] content, HttpRequestInfo httpRequestInfo) {
        MetadataContent manifest = getMetadataContent(namespace, position, contentKey, content, MetadataContent.ResourceType.ENTRY, httpRequestInfo);
        if (LOG.isTraceEnabled()) {
            System.out.printf("%n");
            LOG.trace("ENTRY: {}", manifest.toJSON());
        }

        ContentStream contentStream = new DiscardingContentStream();
        ContentStreamProducer producer = contentStream.producer(namespace);
        ContentStreamBuffer.Builder bufferBuilder = producer.builder();
        bufferBuilder.position(position).buffer(contentKey, content, manifest);

        producer.produce(bufferBuilder);
    }

    @Override
    public void bufferDocument(String namespace, String position, String contentKey, byte[] content, HttpRequestInfo httpRequestInfo) {
        MetadataContent manifest = getMetadataContent(namespace, position, contentKey, content, MetadataContent.ResourceType.DOCUMENT, httpRequestInfo);
        if (LOG.isTraceEnabled()) {
            LOG.trace("DOCUMENT: {}", manifest.toJSON());
        }

        ContentStream contentStream = new DiscardingContentStream();
        ContentStreamProducer producer = contentStream.producer(namespace);
        ContentStreamBuffer.Builder bufferBuilder = producer.builder();
        bufferBuilder.position(position).buffer(contentKey, content, manifest);

        producer.produce(bufferBuilder);
    }

    @Override
    public void publish(String namespace, String... position) {
        for (String pos : position) {
            lastPositionRef.set(pos);
        }
    }

    MetadataContent getMetadataContent(String namespace, String position, String contentKey, byte[] content, MetadataContent.ResourceType resourceType, HttpRequestInfo httpRequestInfo) {
        return new MetadataContent.Builder()
                .resourceType(resourceType)
                .correlationId(httpRequestInfo.getCorrelationIds())
                .url(httpRequestInfo.getUrl())
                .namespace(namespace)
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
