package no.ssb.dc.api;

public interface PositionProducer<T> {

    Position<T> produce(String id);

}
