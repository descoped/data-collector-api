package no.ssb.dc.api.node.impl;

import no.ssb.dc.api.node.Node;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

public abstract class FlowNode extends AbstractBaseNode implements Node {

    static void depthFirstPreOrderFullTraversal(int depth, Set<Node> visitedNodeIds,
                                                List<Node> ancestors, Node currentNode,
                                                BiConsumer<List<Node>, Node> visit) {
        if (!visitedNodeIds.add(currentNode)) {
            return;
        }

        visit.accept(ancestors, currentNode);

        ancestors.add(currentNode);
        try {
            for (Iterator<? extends Node> it = currentNode.iterator(); it.hasNext(); ) {
                depthFirstPreOrderFullTraversal(depth + 1, visitedNodeIds, ancestors, Optional.ofNullable(it.next()).orElseThrow(), visit);
            }
        } finally {
            ancestors.remove(currentNode);
        }
    }

    List<Node> createNodeList() {
        return new ArrayList<>();
    }

    @Override
    public void traverse(int depth, Set<Node> visitedNodeIds, List<Node> ancestors, BiConsumer<List<Node>, Node> visit) {
        depthFirstPreOrderFullTraversal(depth, visitedNodeIds, ancestors, this, visit);
    }

    @Override
    public String toPrintableExecutionPlan() {
        return PrintableExecutionPlan.build(this);
    }
}
