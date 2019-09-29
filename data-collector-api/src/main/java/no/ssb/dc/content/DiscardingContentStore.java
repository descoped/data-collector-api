package no.ssb.dc.content;

import no.ssb.dc.api.content.ContentStore;
import no.ssb.dc.api.content.ContentStream;
import no.ssb.dc.api.content.ContentStreamBuffer;
import no.ssb.dc.api.content.ContentStreamProducer;
import no.ssb.dc.api.content.MetadataContent;
import no.ssb.dc.api.http.Metadata;
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
    public void addPaginationDocument(String namespace, String contentKey, byte[] content, Metadata metadata) {
        ContentStream contentStream = new DiscardingContentStream();
        ContentStreamProducer producer = contentStream.producer(namespace + "-pages");
        ContentStreamBuffer.Builder bufferBuilder = producer.builder();

        String position = metadata.getCorrelationIds().first().toString();
        MetadataContent manifest = getMetadataContent(namespace + "-pages", position, contentKey, content, MetadataContent.ResourceType.PAGE, metadata);
        if (LOG.isTraceEnabled()) {
            LOG.trace("PAGE: {}", manifest.toJSON());
        }

        bufferBuilder.position(position);
        bufferBuilder.buffer(contentKey, content, manifest);

        producer.produce(bufferBuilder);
    }

    @Override
    public void bufferPaginationEntryDocument(String namespace, String position, String contentKey, byte[] content, Metadata metadata) {
        MetadataContent manifest = getMetadataContent(namespace, position, contentKey, content, MetadataContent.ResourceType.ENTRY, metadata);
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
    public void bufferDocument(String namespace, String position, String contentKey, byte[] content, Metadata metadata) {
        MetadataContent manifest = getMetadataContent(namespace, position, contentKey, content, MetadataContent.ResourceType.DOCUMENT, metadata);
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

    MetadataContent getMetadataContent(String namespace, String position, String contentKey, byte[] content, MetadataContent.ResourceType resourceType, Metadata metadata) {
        return new MetadataContent.Builder()
                .resourceType(resourceType)
                .correlationId(metadata.getCorrelationIds())
                .url(metadata.getUrl())
                .namespace(namespace)
                .position(position)
                .contentKey(contentKey)
                .contentType(metadata.getResponseHeaders().firstValue("content-type").orElseGet(() -> "application/octet-stream"))
                .contentLength(content.length)
                .requestDurationNanoTime(metadata.getRequestDurationNanoSeconds())
                .requestHeaders(metadata.getRequestHeaders())
                .responseHeaders(metadata.getResponseHeaders())
                .build();
    }
}
