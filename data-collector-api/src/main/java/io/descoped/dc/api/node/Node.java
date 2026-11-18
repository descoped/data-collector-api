package io.descoped.dc.api.node;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

// implements FlowNode and step-nodes
public interface Node extends Base {

    Configurations configurations();

    Iterator<? extends Node> iterator();

    void traverse(int depth, Set<Node> visitedNodeIds, List<Node> ancestors, BiConsumer<List<Node>, Node> visit);

    String toPrintableExecutionPlan();

}
