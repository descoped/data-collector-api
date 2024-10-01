package io.descoped.dc.api.node;

import java.util.List;

public interface ForEach extends Node {

    Query splitToListQuery();

    List<? extends Node> steps();

}
