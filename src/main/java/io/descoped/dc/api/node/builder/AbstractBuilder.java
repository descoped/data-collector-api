package io.descoped.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Base for node builder classes
 */
public abstract class AbstractBuilder {

    @JsonProperty
    final BuilderType type;

    AbstractBuilder(BuilderType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractBuilder that = (AbstractBuilder) o;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
