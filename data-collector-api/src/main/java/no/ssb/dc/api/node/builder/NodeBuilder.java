package no.ssb.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonInclude;
import no.ssb.dc.api.node.Base;
import no.ssb.dc.api.node.Node;
import no.ssb.dc.api.util.JacksonFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public abstract class NodeBuilder extends AbstractNodeBuilder {

    NodeBuilder(BuilderType type) {
        super(type);
    }

    public String serialize() {
        return JacksonFactory.yamlInstance().toPrettyJSON(this);
    }

    /**
     * Successor is responsible for its own creation and must add itself to nodeInstanceById.
     * Lazy initialization is done through nodeBuilderById.
     *
     * @param buildContext @return
     */
    abstract <R extends Base> R build(BuildContext buildContext);

    public <R extends Base> R build() {
        return build(BuildContext.empty());
    }

    public abstract static class FlowNode extends AbstractBaseNode implements Node {

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
}
