package no.ssb.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public abstract class AbstractNodeBuilder {

    @JsonProperty final BuilderType type;

    AbstractNodeBuilder(BuilderType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractNodeBuilder that = (AbstractNodeBuilder) o;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
