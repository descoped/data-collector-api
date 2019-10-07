package no.ssb.dc.api.content;

public interface ContentStream {

    String lastPosition(String topic);

    ContentStreamProducer producer(String topic);

}
