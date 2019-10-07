package no.ssb.dc.api.handler;

import java.util.List;

public interface QueryFeature {

    enum Type {
        LIST,
        OBJECT,
        STRING_LITERAL;
    }

    byte[] serialize(Object node);

    Object deserialize(byte[] source);

    List<?> evaluateList(Object data);

    Object evaluateObject(Object data);

    String evaluateStringLiteral(Object data);

}
