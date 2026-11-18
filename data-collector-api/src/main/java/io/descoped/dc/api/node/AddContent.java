package io.descoped.dc.api.node;

import java.util.Map;

public interface AddContent extends Node {

    String positionVariableExpression();

    String contentKey();

    Map<String, Object> state();

}
