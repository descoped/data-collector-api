package no.ssb.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.ssb.dc.api.node.Base;
import no.ssb.dc.api.util.JacksonFactory;

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
     * Lazy initialization is done through nodeBuilderById.
     *
     * @param buildContext @return
     */
    abstract <R extends Base> R build(BuildContext buildContext);

    public <R extends Base> R build() {
        return build(BuildContext.empty());
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
