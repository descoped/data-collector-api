package no.ssb.dc.api;

public class DefaultStringPositionProducer implements PositionProducer<String> {

    @Override
    public Position<String> produce(String id) {
        return new Position<>(id);
    }
}
