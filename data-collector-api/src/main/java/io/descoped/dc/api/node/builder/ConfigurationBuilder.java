package io.descoped.dc.api.node.builder;

import io.descoped.dc.api.node.Configuration;

public abstract class ConfigurationBuilder extends LeafNodeBuilder {

    ConfigurationBuilder(BuilderType type) {
        super(type);
    }

    abstract static class ConfigurationNode extends LeafNode implements Configuration {

    }
}
