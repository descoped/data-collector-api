package io.descoped.dc.api.handler;

import java.util.List;

public interface QueryFeature {

    enum Type {
        LIST,
        OBJECT,
        STRING_LITERAL;
    }

    List<?> evaluateList(Object data);

    Object evaluateObject(Object data);

    String evaluateStringLiteral(Object data);

}
