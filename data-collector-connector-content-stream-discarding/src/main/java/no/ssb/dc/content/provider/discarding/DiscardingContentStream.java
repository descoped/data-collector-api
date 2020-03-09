package no.ssb.dc.content.provider.discarding;

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
    public String lastPosition(String topic) {
        return null;
    }

    @Override
    public ContentStreamProducer producer(String topic) {
        return new DiscardingContentProducer();
    }

    @Override
    public void closeAndRemoveProducer(String topic) {
    }

    @Override
    public void close() throws Exception {

    }
}
