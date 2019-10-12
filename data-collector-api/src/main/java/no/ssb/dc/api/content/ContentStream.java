package no.ssb.dc.api.content;

public interface ContentStream extends AutoCloseable {

    String lastPosition(String topic);

    ContentStreamProducer producer(String topic);

}
