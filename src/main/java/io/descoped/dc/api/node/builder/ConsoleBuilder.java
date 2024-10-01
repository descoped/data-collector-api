package io.descoped.dc.api.node.builder;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.descoped.dc.api.node.Base;
import io.descoped.dc.api.node.Configurations;
import io.descoped.dc.api.node.Console;
import io.descoped.dc.api.node.Node;

import java.util.Iterator;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class ConsoleBuilder extends NodeBuilder {

    public ConsoleBuilder() {
        super(BuilderType.Console);
    }

    @Override
    <R extends Base> R build(BuildContext buildContext) {
        return (R) new ConsoleNode(buildContext.getInstance(SpecificationBuilder.GLOBAL_CONFIGURATION));
    }

    @Override
    public String toString() {
        return "ConsoleBuilder{}";
    }

    static class ConsoleNode extends FlowNode implements Console {

        public ConsoleNode(Configurations configurations) {
            super(configurations);
        }

        @Override
        public void log() {

        }

        @Override
        public Iterator<? extends Node> iterator() {
            return createNodeList().iterator();
        }

        @Override
        public String toString() {
            return "ConsoleNode{}";
        }
    }
}
