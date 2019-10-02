package no.ssb.dc.api.node.builder;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cache build instances during build and use instance map to lookup already created nodes.
 * E.g. the PaginateBuilder creates GetNode and put it to the nodeInstanceById Map. During build in Flow.end()
 * the GetNode instance is resolved and referenced.
 *
 * The BuildContext is ignored by leaf nodes unless the node instance should be cached.
 */
class BuildContext {
    private final Map<String, NodeBuilder> nodeBuilderById;
    private final Map<String, ?> nodeInstanceById;

    /**
     * @param nodeBuilderById
     * @param nodeInstanceById
     */
    private BuildContext(Map<String, NodeBuilder> nodeBuilderById, Map<String, ?> nodeInstanceById) {
        this.nodeBuilderById = nodeBuilderById;
        this.nodeInstanceById = nodeInstanceById;
    }

    static BuildContext fromNodeBuilderById(Map<String, NodeBuilder> nodeBuilderById) {
        return new BuildContext(nodeBuilderById, new LinkedHashMap<>());
    }

    static BuildContext empty() {
        return new BuildContext(new LinkedHashMap<>(), new LinkedHashMap<>());
    }

    Map<String, NodeBuilder> nodeBuilderById() {
        return nodeBuilderById;
    }

    <R> Map<String, R> nodeInstanceById() {
        return (Map<String, R>) nodeInstanceById;
    }
}
