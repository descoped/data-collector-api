package no.ssb.dc.content.provider.discarding;

import no.ssb.dc.api.content.ContentStreamBuffer;
import no.ssb.dc.api.content.ContentStreamProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscardingContentProducer implements ContentStreamProducer {

    private final static Logger LOG = LoggerFactory.getLogger(DiscardingContentProducer.class);

    @Override
    public ContentStreamBuffer.Builder builder() {
        return new DiscardingContentStreamBuffer.Builder();
    }

    @Override
    public void produce(ContentStreamBuffer.Builder bufferBuilder) {

    }

    @Override
    public void publish(String... position) {

    }

    @Override
    public void close() throws Exception {

    }
}
