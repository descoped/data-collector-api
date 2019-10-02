package no.ssb.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import no.ssb.dc.api.node.NodeWithId;

import java.util.Objects;

public abstract class NodeWithIdBuilder extends NodeBuilder {

    @JsonProperty String id;

    NodeWithIdBuilder(BuilderType type) {
        super(type);
    }

    String getId() {
        Objects.requireNonNull(id);
        return id;
    }

    void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NodeWithIdBuilder that = (NodeWithIdBuilder) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id);
    }

    @Override
    public String toString() {
        return "NodeWithIdBuilder{" +
                "id='" + id + '\'' +
                '}';
    }

    public abstract static class FlowNodeWithId extends FlowNode implements NodeWithId {
        final String id;

        FlowNodeWithId(String id) {
            this.id = id;
        }

        @Override
        public String id() {
            return id;
        }
    }
}
