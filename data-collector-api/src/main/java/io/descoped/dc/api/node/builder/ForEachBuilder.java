package io.descoped.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.descoped.dc.api.node.Base;
import io.descoped.dc.api.node.Configurations;
import io.descoped.dc.api.node.ForEach;
import io.descoped.dc.api.node.Node;
import io.descoped.dc.api.node.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class ForEachBuilder extends NodeBuilder {

    @JsonProperty("splitQuery")
    QueryBuilder splitBuilder;
    @JsonProperty("pipes")
    List<NodeBuilder> pipes = new ArrayList<>();

    public ForEachBuilder(QueryBuilder splitBuilder) {
        super(BuilderType.ForEach);
        this.splitBuilder = splitBuilder;
    }

    public ForEachBuilder pipe(NodeBuilder builder) {
        pipes.add(builder);
        return this;
    }

    @Override
    <R extends Base> R build(BuildContext buildContext) {
        QueryBuilder.QueryNode splitToListQueryNode = splitBuilder.build(buildContext);

        List<Node> stepNodeList = new ArrayList<>();
        for (NodeBuilder stepBuilder : pipes) {
            Node stepNode = stepBuilder.build(buildContext);
            stepNodeList.add(stepNode);
        }

        return (R) new ForEachNode(buildContext.getInstance(SpecificationBuilder.GLOBAL_CONFIGURATION), splitToListQueryNode, stepNodeList);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ForEachBuilder builder = (ForEachBuilder) o;
        return Objects.equals(splitBuilder, builder.splitBuilder) &&
                Objects.equals(pipes, builder.pipes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), splitBuilder, pipes);
    }

    @Override
    public String toString() {
        return "ForEachBuilder{" +
                "splitBuilder=" + splitBuilder +
                ", pipes=" + pipes +
                '}';
    }

    static class ForEachNode extends FlowNode implements ForEach {

        final QueryBuilder.QueryNode splitNode;
        final List<Node> stepNodeList;

        public ForEachNode(Configurations configurations, QueryBuilder.QueryNode splitNode, List<Node> stepNodeList) {
            super(configurations);
            this.splitNode = splitNode;
            this.stepNodeList = stepNodeList;
        }

        @Override
        public Query splitToListQuery() {
            return splitNode;
        }

        @Override
        public List<? extends Node> steps() {
            return stepNodeList;
        }

        @Override
        public java.util.Iterator<? extends Node> iterator() {
            return createNodeList().iterator();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ForEachNode that = (ForEachNode) o;
            return Objects.equals(splitNode, that.splitNode) &&
                    Objects.equals(stepNodeList, that.stepNodeList);
        }

        @Override
        public int hashCode() {
            return Objects.hash(splitNode, stepNodeList);
        }

        @Override
        public String toString() {
            return "ForEachNode{" +
                    "splitNode=" + splitNode +
                    ", stepNodeList=" + stepNodeList +
                    '}';
        }
    }
}
