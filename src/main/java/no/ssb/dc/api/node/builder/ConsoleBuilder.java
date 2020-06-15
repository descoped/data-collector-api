package no.ssb.dc.api.node.builder;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import no.ssb.dc.api.node.Base;
import no.ssb.dc.api.node.Configurations;
import no.ssb.dc.api.node.Console;
import no.ssb.dc.api.node.Node;

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
    }
}
