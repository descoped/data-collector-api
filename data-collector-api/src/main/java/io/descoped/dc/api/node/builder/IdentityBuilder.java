package io.descoped.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;

abstract public class IdentityBuilder extends LeafNodeBuilder {

    @JsonProperty
    String id;

    IdentityBuilder(BuilderType type, String id) {
        super(type);
        this.id = id;
    }

    abstract public class IdentityNode extends LeafNode {

        protected final String id;

        public IdentityNode(String id) {
            this.id = id;
        }
    }
}
