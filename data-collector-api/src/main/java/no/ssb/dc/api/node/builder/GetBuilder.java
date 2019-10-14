package no.ssb.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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
    @JsonProperty("pipes") List<NodeBuilder> pipes = new ArrayList<>();
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

    public GetBuilder pipe(NodeBuilder builder) {
        pipes.add(builder);
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
        for (NodeBuilder stepBuilder : pipes) {
            Node stepNode = stepBuilder.build(buildContext);
            stepNodeList.add(stepNode);
        }

        return (R) new GetNode(getId(), buildContext.getInstance(SpecificationBuilder.GLOBAL_CONFIGURATION), url, requestHeaders, validators, stepNodeList, returnVariables);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GetBuilder that = (GetBuilder) o;
        return Objects.equals(requestHeaders, that.requestHeaders) &&
                Objects.equals(validators, that.validators) &&
                Objects.equals(pipes, that.pipes) &&
                Objects.equals(returnVariables, that.returnVariables);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), requestHeaders, validators, pipes, returnVariables);
    }

    @Override
    public String toString() {
        return "GetBuilder{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", requestHeaders=" + requestHeaders +
                ", validators=" + validators +
                ", pipes=" + pipes +
                ", returnVariables=" + returnVariables +
                '}';
    }

    static class GetNode extends OperationNode implements Get {

        final String url;
        final Headers headers;
        private final List<Validator> validateResponse;
        final List<Node> pipes;
        final List<String> returnVariables;

        GetNode(String id, Configurations configurations, String url, Headers headers, List<Validator> validateResponse, List<Node> pipes, List<String> returnVariables) {
            super(configurations, id);
            this.url = url;
            this.headers = headers;
            this.validateResponse = validateResponse;
            this.pipes = pipes;
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
            return pipes;
        }

        @Override
        public List<String> returnVariables() {
            return returnVariables;
        }

        @Override
        public Iterator<? extends Node> iterator() {
            return pipes.iterator();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GetNode getNode = (GetNode) o;
            return Objects.equals(url, getNode.url) &&
                    Objects.equals(headers, getNode.headers) &&
                    Objects.equals(validateResponse, getNode.validateResponse) &&
                    Objects.equals(pipes, getNode.pipes) &&
                    Objects.equals(returnVariables, getNode.returnVariables);
        }

        @Override
        public int hashCode() {
            return Objects.hash(url, headers, validateResponse, pipes, returnVariables);
        }

        @Override
        public String toString() {
            return "GetNode{" +
                    "id='" + id + '\'' +
                    ", url='" + url + '\'' +
                    ", headers=" + headers +
                    ", validateResponse=" + validateResponse +
                    ", steps=" + pipes +
                    ", returnVariables=" + returnVariables +
                    '}';
        }
    }
}
