package no.ssb.dc.api.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import no.ssb.dc.api.BuilderType;

public abstract class AbstractNodeBuilder {

    @JsonProperty final BuilderType type;

    AbstractNodeBuilder(BuilderType type) {
        this.type = type;
    }
}
