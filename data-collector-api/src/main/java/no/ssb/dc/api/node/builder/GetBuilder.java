package no.ssb.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import no.ssb.dc.api.PositionProducer;
import no.ssb.dc.api.http.Headers;
import no.ssb.dc.api.node.Base;
import no.ssb.dc.api.node.Configurations;
import no.ssb.dc.api.node.Get;
import no.ssb.dc.api.node.Node;
import no.ssb.dc.api.node.Validator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class GetBuilder extends OperationBuilder {

    @JsonProperty Headers requestHeaders = new Headers();
    @JsonProperty("responseValidators") List<LeafNodeBuilder> validators = new ArrayList<>();
    @JsonProperty List<NodeBuilder> steps = new ArrayList<>();
    @JsonProperty Class<? extends PositionProducer> positionProducerClass;
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

    public GetBuilder header(String name, String value) {
        requestHeaders.put(name, value);
        return this;
    }

    public GetBuilder validate(LeafNodeBuilder validationBuilder) {
        validators.add(validationBuilder);
        return this;
    }

    public GetBuilder step(NodeBuilder builder) {
        steps.add(builder);
        return this;
    }

    public GetBuilder positionProducer(Class<? extends PositionProducer> producerClass) {
        this.positionProducerClass = producerClass;
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
    <R extends Base> R build(BuildContext buildContext) {
        List<Validator> validators = new ArrayList<>();
        for (LeafNodeBuilder validatorBuilder : this.validators) {
            Validator validator = validatorBuilder.build(buildContext);
            validators.add(validator);
        }

        List<Node> stepNodeList = new ArrayList<>();
        for (NodeBuilder stepBuilder : steps) {
            Node stepNode = stepBuilder.build(buildContext);
            stepNodeList.add(stepNode);
        }

        return (R) new GetNode(getId(), buildContext.getInstance(FlowBuilder.GLOBAL_CONFIGURATION), url, requestHeaders, validators, stepNodeList, positionProducerClass, returnVariables);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GetBuilder that = (GetBuilder) o;
        return Objects.equals(requestHeaders, that.requestHeaders) &&
                Objects.equals(validators, that.validators) &&
                Objects.equals(steps, that.steps) &&
                Objects.equals(positionProducerClass, that.positionProducerClass) &&
                Objects.equals(returnVariables, that.returnVariables);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), requestHeaders, validators, steps, positionProducerClass, returnVariables);
    }

    @Override
    public String toString() {
        return "GetBuilder{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", requestHeaders=" + requestHeaders +
                ", validators=" + validators +
                ", steps=" + steps +
                ", positionProducerClass=" + positionProducerClass +
                ", returnVariables=" + returnVariables +
                '}';
    }

    static class GetNode extends OperationNode implements Get {

        final String url;
        final Headers headers;
        private final List<Validator> validateResponse;
        final List<Node> steps;
        final Class<? extends PositionProducer> positionProducerClass;
        final List<String> returnVariables;

        GetNode(String id, Configurations configurations, String url, Headers headers, List<Validator> validateResponse, List<Node> steps, Class<? extends PositionProducer> positionProducerClass, List<String> returnVariables) {
            super(configurations, id);
            this.url = url;
            this.headers = headers;
            this.validateResponse = validateResponse;
            this.steps = steps;
            this.positionProducerClass = positionProducerClass;
            this.returnVariables = returnVariables;
        }

        @Override
        public String url() {
            return url;
        }

        @Override
        public Headers headers() {
            return headers;
        }

        @Override
        public List<Validator> responseValidators() {
            return validateResponse;
        }

        @Override
        public List<? extends Node> steps() {
            return steps;
        }

        @Override
        public Class<? extends PositionProducer> positionProducerClass() {
            return positionProducerClass;
        }

        @Override
        public List<String> returnVariables() {
            return returnVariables;
        }

        @Override
        public Iterator<? extends Node> iterator() {
            return steps.iterator();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GetNode getNode = (GetNode) o;
            return Objects.equals(url, getNode.url) &&
                    Objects.equals(headers, getNode.headers) &&
                    Objects.equals(validateResponse, getNode.validateResponse) &&
                    Objects.equals(steps, getNode.steps) &&
                    Objects.equals(positionProducerClass, getNode.positionProducerClass) &&
                    Objects.equals(returnVariables, getNode.returnVariables);
        }

        @Override
        public int hashCode() {
            return Objects.hash(url, headers, validateResponse, steps, positionProducerClass, returnVariables);
        }

        @Override
        public String toString() {
            return "GetNode{" +
                    "id='" + id + '\'' +
                    ", url='" + url + '\'' +
                    ", headers=" + headers +
                    ", validateResponse=" + validateResponse +
                    ", steps=" + steps +
                    ", positionProducerClass=" + positionProducerClass +
                    ", returnVariables=" + returnVariables +
                    '}';
        }
    }
}
