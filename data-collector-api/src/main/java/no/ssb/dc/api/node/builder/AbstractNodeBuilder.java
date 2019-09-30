package no.ssb.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class AbstractNodeBuilder {

    @JsonProperty final BuilderType type;

    AbstractNodeBuilder(BuilderType type) {
        this.type = type;
    }

}
