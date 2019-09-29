package no.ssb.dc.api.builder;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class OperationBuilder extends NodeBuilder {

    @JsonProperty String url;

    OperationBuilder(BuilderType type) {
        super(type);
    }
}
