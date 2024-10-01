package io.descoped.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.descoped.dc.api.node.Base;
import io.descoped.dc.api.node.Configurations;
import io.descoped.dc.api.node.NextPage;
import io.descoped.dc.api.node.Node;
import io.descoped.dc.api.node.Query;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class NextPageBuilder extends NodeBuilder {

    @JsonProperty("outputs")
    Map<String, QueryBuilder> outputMap = new LinkedHashMap<>();

    public NextPageBuilder() {
        super(BuilderType.NextPage);
    }

    public NextPageBuilder output(String variable, QueryBuilder queryBuilder) {
        outputMap.put(variable, queryBuilder);
        return this;
    }

    @Override
    <R extends Base> R build(BuildContext buildContext) {
        Map<String, QueryBuilder.QueryNode> queryNodeMap = new LinkedHashMap<>();
        for (Map.Entry<String, QueryBuilder> entry : outputMap.entrySet()) {
            queryNodeMap.put(entry.getKey(), entry.getValue().build(buildContext));
        }
        return (R) new NextPageNode(buildContext.getInstance(SpecificationBuilder.GLOBAL_CONFIGURATION), queryNodeMap);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NextPageBuilder that = (NextPageBuilder) o;
        return Objects.equals(outputMap, that.outputMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), outputMap);
    }

    @Override
    public String toString() {
        return "NextPageBuilder{" +
                "outputMap=" + outputMap +
                '}';
    }

    static class NextPageNode extends FlowNode implements NextPage {

        private final Map<String, QueryBuilder.QueryNode> queryNodeMap;

        NextPageNode(Configurations configurations, Map<String, QueryBuilder.QueryNode> queryNodeMap) {
            super(configurations);
            this.queryNodeMap = queryNodeMap;
        }

        @Override
        public Map<String, Query> outputs() {
            return queryNodeMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> (Query) y, LinkedHashMap::new));
        }

        @Override
        public Iterator<? extends Node> iterator() {
            return createNodeList().iterator();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NextPageNode that = (NextPageNode) o;
            return Objects.equals(queryNodeMap, that.queryNodeMap);
        }

        @Override
        public int hashCode() {
            return Objects.hash(queryNodeMap);
        }

        @Override
        public String toString() {
            return "NextPageNode{" +
                    "queryNodeMap=" + queryNodeMap +
                    '}';
        }
    }
}
