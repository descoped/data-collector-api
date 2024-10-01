package io.descoped.dc.api.handler;

import java.io.InputStream;
import java.util.function.Consumer;

public interface DocumentParserFeature {

    byte[] serialize(Object document);

    Object deserialize(byte[] source);

    void tokenDeserializer(InputStream source, Consumer<Object> entryCallback);

}
