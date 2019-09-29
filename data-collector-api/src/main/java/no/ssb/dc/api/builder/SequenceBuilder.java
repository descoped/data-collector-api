package no.ssb.dc.api.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import no.ssb.dc.api.NodeBuilderDeserializer;
import no.ssb.dc.api.node.BaseNode;
import no.ssb.dc.api.node.impl.QueryNode;
import no.ssb.dc.api.node.impl.SequenceNode;

import java.util.Map;
import java.util.Objects;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class SequenceBuilder extends NodeBuilder {

    @JsonProperty("splitQuery") QueryBuilder splitBuilder;
    @JsonProperty("expectedQuery") QueryBuilder expectedBuilder;

    public SequenceBuilder(QueryBuilder splitBuilder) {
        super(BuilderType.Sequence);
        this.splitBuilder = splitBuilder;
    }

    public void splitBuilder(QueryBuilder splitBuilder) {
        this.splitBuilder = splitBuilder;
    }

    public SequenceBuilder expected(QueryBuilder expectedBuilder) {
        this.expectedBuilder = expectedBuilder;
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    <R extends BaseNode> R build(Map<String, NodeBuilder> nodeBuilderById, Map<String, R> nodeInstanceById) {
        QueryNode splitToListQueryNode = (QueryNode) splitBuilder.build(nodeBuilderById, nodeInstanceById);
        QueryNode splitCriteriaQueryNode = (QueryNode) expectedBuilder.build(nodeBuilderById, nodeInstanceById);
        return (R) new SequenceNode(splitToListQueryNode, splitCriteriaQueryNode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SequenceBuilder)) return false;
        SequenceBuilder that = (SequenceBuilder) o;
        return Objects.equals(splitBuilder, that.splitBuilder) &&
                Objects.equals(expectedBuilder, that.expectedBuilder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(splitBuilder, expectedBuilder);
    }

    @Override
    public String toString() {
        return "SequenceBuilder{" +
                "splitBuilder=" + splitBuilder +
                ", expectedBuilder=" + expectedBuilder +
                '}';
    }
}
