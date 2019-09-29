package no.ssb.dc.api.builder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.ssb.dc.api.BuilderType;
import no.ssb.dc.api.node.BaseNode;
import no.ssb.dc.api.util.JacksonFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public abstract class NodeBuilder extends AbstractNodeBuilder {

    @JsonProperty String id;

    NodeBuilder(BuilderType type) {
        super(type);
    }

    String getId() {
        Objects.requireNonNull(id);
        return id;
    }

    void setId(String id) {
        this.id = id;
    }

    public String serialize() {
        return JacksonFactory.yamlInstance().toPrettyJSON(this);
    }

    /**
     * Successor is responsible for its own creation and must add itself to nodeInstanceById.
     * Lazy initialization is dont through nodeBuilderById.
     *
     * @param nodeBuilderById
     * @param nodeInstanceById
     * @return
     */
    abstract <R extends BaseNode> R build(Map<String, NodeBuilder> nodeBuilderById, Map<String, R> nodeInstanceById);

    public <R extends BaseNode> R build() {
        return build(new LinkedHashMap<>(), new LinkedHashMap<>());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeBuilder)) return false;
        NodeBuilder that = (NodeBuilder) o;
        return type == that.type &&
                Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, getId());
    }

    @Override
    public String toString() {
        return "NodeBuilder{" +
                "type=" + type +
                ", id='" + id + '\'' +
                '}';
    }
}
