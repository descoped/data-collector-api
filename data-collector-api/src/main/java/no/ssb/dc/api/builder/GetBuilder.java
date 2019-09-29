package no.ssb.dc.api.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import no.ssb.dc.api.NodeBuilderDeserializer;
import no.ssb.dc.api.node.BaseNode;
import no.ssb.dc.api.node.Node;
import no.ssb.dc.api.node.impl.GetNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class GetBuilder extends OperationBuilder {

    @JsonProperty List<NodeBuilder> steps = new ArrayList<>();
    @JsonProperty List<String> returnVariables = new ArrayList<>();

    GetBuilder() {
        super(BuilderType.Get);
    }

    public GetBuilder(String id) {
        super(BuilderType.Get);
        setId(id);
    }

    public GetBuilder id(String id) {
        setId(id);
        return this;
    }

    public GetBuilder url(String urlString) {
        this.url = urlString;
        return this;
    }

    public GetBuilder step(NodeBuilder builder) {
        steps.add(builder);
        return this;
    }

    public GetBuilder returnVariables(String... variableKeys) {
        for (String variableKey : variableKeys) {
            returnVariables.add(variableKey);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    <R extends BaseNode> R build(Map<String, NodeBuilder> nodeBuilderById, Map<String, R> nodeInstanceById) {
        List<Node> stepNodeList = new ArrayList<>();
        for (NodeBuilder stepBuilder : steps) {
            Node stepNode = (Node) stepBuilder.build(nodeBuilderById, nodeInstanceById);
            stepNodeList.add(stepNode);
        }
        return (R) new GetNode(getId(), url, stepNodeList, returnVariables);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GetBuilder that = (GetBuilder) o;
        return Objects.equals(steps, that.steps) &&
                Objects.equals(returnVariables, that.returnVariables);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), steps, returnVariables);
    }

    @Override
    public String toString() {
        return "GetBuilder{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", steps=" + steps +
                '}';
    }
}
