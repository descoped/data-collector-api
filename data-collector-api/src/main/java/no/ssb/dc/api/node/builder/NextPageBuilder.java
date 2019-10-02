package no.ssb.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import no.ssb.dc.api.node.Base;
import no.ssb.dc.api.node.NextPage;
import no.ssb.dc.api.node.Node;
import no.ssb.dc.api.node.Query;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
    <R extends Base> R build(BuildContext buildContext) {
        Map<String, QueryBuilder.QueryNode> queryNodeMap = new LinkedHashMap<>();
        for (Map.Entry<String, QueryBuilder> entry : outputMap.entrySet()) {
            queryNodeMap.put(entry.getKey(), entry.getValue().build(buildContext));
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

    static class NextPageNode extends FlowNode implements NextPage {

        private final Map<String, QueryBuilder.QueryNode> queryNodeMap;

        NextPageNode(Map<String, QueryBuilder.QueryNode> queryNodeMap) {
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
    }
}
