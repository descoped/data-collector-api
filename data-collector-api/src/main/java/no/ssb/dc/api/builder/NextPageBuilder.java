package no.ssb.dc.api.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import no.ssb.dc.api.BuilderType;
import no.ssb.dc.api.NodeBuilderDeserializer;
import no.ssb.dc.api.node.BaseNode;
import no.ssb.dc.api.node.impl.NextPageNode;
import no.ssb.dc.api.node.impl.QueryNode;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class NextPageBuilder extends NodeBuilder {

    @JsonProperty("outputs") Map<String, QueryBuilder> outputMap = new LinkedHashMap<>();

    public NextPageBuilder() {
        super(BuilderType.NextPage);
    }

    public NextPageBuilder output(String variable, QueryBuilder queryBuilder) {
        outputMap.put(variable, queryBuilder);
        return this;
    }

    @Override
    <R extends BaseNode> R build(Map<String, NodeBuilder> nodeBuilderById, Map<String, R> nodeInstanceById) {
        Map<String, QueryNode> queryNodeMap = new LinkedHashMap<>();
        for (Map.Entry<String, QueryBuilder> entry : outputMap.entrySet()) {
            queryNodeMap.put(entry.getKey(), (QueryNode) entry.getValue().build(nodeBuilderById, nodeInstanceById));
        }
        return (R) new NextPageNode(queryNodeMap);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NextPageBuilder)) return false;
        NextPageBuilder builder = (NextPageBuilder) o;
        return Objects.equals(outputMap, builder.outputMap);
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
}
