package io.descoped.dc.api.node.builder;

import io.descoped.dc.api.node.Base;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Cache build instances during build and use instance map to lookup already created nodes.
 * E.g. the PaginateBuilder creates GetNode and put it to the nodeInstanceById Map. During build in Flow.end()
 * the GetNode instance is resolved and referenced.
 * <p>
 * The BuildContext is ignored by leaf nodes unless the node instance should be cached.
 */
public class BuildContext {
    private final Map<String, NodeBuilder> nodeBuilderById;
    private final Map<String, Object> nodeInstanceById;

    /**
     * @param nodeBuilderById
     * @param nodeInstanceById
     */
    private BuildContext(Map<String, NodeBuilder> nodeBuilderById, Map<String, Object> nodeInstanceById) {
        this.nodeBuilderById = nodeBuilderById;
        this.nodeInstanceById = nodeInstanceById;
    }

    static BuildContext fromNodeBuilderById(Map<String, NodeBuilder> nodeBuilderById) {
        return new BuildContext(nodeBuilderById, new LinkedHashMap<>());
    }

    public static BuildContext empty() {
        return new BuildContext(new LinkedHashMap<>(), new LinkedHashMap<>());
    }

    public static BuildContext of(Map<String, NodeBuilder> nodeBuilderById, Map<String, Object> nodeInstanceById) {
        return new BuildContext(nodeBuilderById, nodeInstanceById);
    }

    Map<String, NodeBuilder> nodeBuilderById() {
        return nodeBuilderById;
    }

    NodeBuilder getBuilder(String id) {
        return nodeBuilderById.get(id);
    }

    boolean containsBuilder(String id) {
        return nodeBuilderById.containsKey(id);
    }

    <R> Map<String, R> nodeInstanceById() {
        return (Map<String, R>) nodeInstanceById;
    }

    <R> R getInstance(String id) {
        return (R) nodeInstanceById.get(id);
    }

    boolean containsInstance(String id) {
        return nodeInstanceById.containsKey(id);
    }

    void cacheInstance(String id, Object instance) {
        nodeInstanceById.put(id, instance);
    }

    <V extends Base> V cacheInstanceIfAbsent(String key, Function<? super String, V> mappingFunction) {
        return (V) nodeInstanceById.computeIfAbsent(key, mappingFunction);
    }

    public boolean isEmpty() {
        return nodeBuilderById.isEmpty() && nodeInstanceById.isEmpty();
    }
}
