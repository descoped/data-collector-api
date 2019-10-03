package no.ssb.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonInclude;
import no.ssb.dc.api.node.Base;
import no.ssb.dc.api.node.Leaf;
import no.ssb.dc.api.util.JacksonFactory;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public abstract class LeafNodeBuilder extends AbstractBuilder {

    LeafNodeBuilder(BuilderType type) {
        super(type);
    }

    public String serialize() {
        return JacksonFactory.yamlInstance().toPrettyJSON(this);
    }

    /**
     * Successor is responsible for its own creation and must add itself to nodeInstanceById.
     * Lazy initialization is done through nodeBuilderById.
     *
     * @param buildContext @return
     */
    abstract <R extends Base> R build(BuildContext buildContext);

    public <R extends Base> R build() {
        return build(BuildContext.empty());
    }

    abstract static class LeafNode extends AbstractBaseNode implements Leaf {
    }
}
