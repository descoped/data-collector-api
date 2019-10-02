package no.ssb.dc.api.node.builder;

import java.util.Map;

public class BuildContext {
    public final Map<String, NodeBuilder> nodeBuilderById;
    public final Map<String, ?> nodeInstanceById;

    /**
     * @param nodeBuilderById
     * @param nodeInstanceById
     */
    public BuildContext(Map<String, NodeBuilder> nodeBuilderById, Map<String, ?> nodeInstanceById) {
        this.nodeBuilderById = nodeBuilderById;
        this.nodeInstanceById = nodeInstanceById;
    }

    public Map<String, NodeBuilder> getNodeBuilderById() {
        return nodeBuilderById;
    }

    public <R> Map<String, R> getNodeInstanceById() {
        return (Map<String, R>) nodeInstanceById;
    }
}
