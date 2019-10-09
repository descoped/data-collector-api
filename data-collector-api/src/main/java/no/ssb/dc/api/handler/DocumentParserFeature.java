package no.ssb.dc.api.handler;

public interface DocumentParserFeature {

    byte[] serialize(Object document);

    Object deserialize(byte[] source);

}
