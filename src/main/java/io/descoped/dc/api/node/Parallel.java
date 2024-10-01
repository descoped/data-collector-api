package io.descoped.dc.api.node;

import java.util.List;
import java.util.Set;

public interface Parallel extends Node {

    Query splitQuery();

    Set<String> variableNames();

    Query variable(String name);

    List<Node> steps();

    Publish publish();

}
