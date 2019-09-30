package no.ssb.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import no.ssb.dc.api.node.Base;
import no.ssb.dc.api.node.Node;
import no.ssb.dc.api.node.Sequence;

import java.util.Iterator;
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
    <R extends Base> R build(Map<String, NodeBuilder> nodeBuilderById, Map<String, R> nodeInstanceById) {
        QueryBuilder.QueryNode splitToListQueryNode = (QueryBuilder.QueryNode) splitBuilder.build(nodeBuilderById, nodeInstanceById);
        QueryBuilder.QueryNode splitCriteriaQueryNode = (QueryBuilder.QueryNode) expectedBuilder.build(nodeBuilderById, nodeInstanceById);
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

    static class SequenceNode extends FlowNode implements Sequence {

        final QueryBuilder.QueryNode splitNode;
        final QueryBuilder.QueryNode expectedNode;

        SequenceNode(QueryBuilder.QueryNode splitNode, QueryBuilder.QueryNode expectedNode) {
            this.splitNode = splitNode;
            this.expectedNode = expectedNode;
        }

        @Override
        public QueryBuilder.QueryNode splitToListQuery() {
            return splitNode;
        }

        @Override
        public QueryBuilder.QueryNode expectedQuery() {
            return expectedNode;
        }

        @Override
        public Iterator<? extends Node> iterator() {
            return createNodeList().iterator();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SequenceNode that = (SequenceNode) o;
            return Objects.equals(splitNode, that.splitNode) &&
                    Objects.equals(expectedNode, that.expectedNode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(splitNode, expectedNode);
        }

        @Override
        public String toString() {
            return "SequenceNode{" +
                    "splitNode=" + splitNode +
                    ", expectedNode=" + expectedNode +
                    '}';
        }
    }
}