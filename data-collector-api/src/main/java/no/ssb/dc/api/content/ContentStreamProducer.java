package no.ssb.dc.api.content;

public interface ContentStreamProducer extends AutoCloseable {

    ContentStreamBuffer.Builder builder();

    void produce(ContentStreamBuffer.Builder bufferBuilder);

    void publish(String... position);

}
