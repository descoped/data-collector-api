package no.ssb.dc.content;

import no.ssb.dc.api.content.ContentStream;
import no.ssb.dc.api.content.ContentStreamBuffer;
import no.ssb.dc.api.content.ContentStreamProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DiscardingContentStream implements ContentStream {

    private final static Logger LOG = LoggerFactory.getLogger(DiscardingContentStream.class);

    final Map<String, ContentStreamBuffer> buffersByName = new ConcurrentHashMap<>();


    @Override
    public String lastPosition(String namespace) {
        return null;
    }

    @Override
    public ContentStreamProducer producer(String namespace) {
        return new DiscardingContentProducer();
    }
}
