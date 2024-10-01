package io.descoped.dc.api.node;

import java.util.List;
import java.util.Map;

public interface Execute extends Node {

    String executeId();

    List<String> requiredInputs();

    Map<String, Query> inputVariable();

    NodeWithId target();

}
