package no.ssb.dc.content.provider.discarding;

import no.ssb.dc.api.content.ContentStreamBuffer;
import no.ssb.dc.api.content.ContentStreamProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscardingContentStreamProducer implements ContentStreamProducer {

    private final static Logger LOG = LoggerFactory.getLogger(DiscardingContentStreamProducer.class);

    @Override
    public ContentStreamBuffer.Builder builder() {
        return new DiscardingContentStreamBuffer.Builder();
    }

    @Override
    public ContentStreamProducer produce(ContentStreamBuffer.Builder bufferBuilder) {
        return this;
    }

    @Override
    public void publish(String... position) {

    }

    @Override
    public void close() throws Exception {

    }
}
