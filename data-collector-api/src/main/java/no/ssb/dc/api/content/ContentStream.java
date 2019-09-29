package no.ssb.dc.api.content;

public interface ContentStream {

    String lastPosition(String namespace);

    ContentStreamProducer producer(String namespace);

}
