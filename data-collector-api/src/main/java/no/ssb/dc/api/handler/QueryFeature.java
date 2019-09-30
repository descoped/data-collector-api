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

    List<?> queryList(Object data);

    Object queryObject(Object data);

    String queryStringLiteral(Object data);

}
