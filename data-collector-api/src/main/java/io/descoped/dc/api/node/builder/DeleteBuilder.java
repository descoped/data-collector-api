package io.descoped.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.descoped.dc.api.http.Headers;
import io.descoped.dc.api.node.Base;
import io.descoped.dc.api.node.Configurations;
import io.descoped.dc.api.node.Delete;
import io.descoped.dc.api.node.Node;
import io.descoped.dc.api.node.Validator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static io.descoped.dc.api.Builders.status;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class DeleteBuilder extends OperationBuilder {

    @JsonUnwrapped(prefix = "request")
    Headers requestHeaders = new Headers();
    @JsonProperty("responseValidators")
    List<LeafNodeBuilder> validators = new ArrayList<>();
    @JsonProperty("pipes")
    List<NodeBuilder> pipes = new ArrayList<>();
    @JsonProperty
    List<String> returnVariables = new ArrayList<>();

    DeleteBuilder() {
        super(BuilderType.Delete);
    }

    public DeleteBuilder(String id) {
        super(BuilderType.Delete);
        setId(id);
    }

    public DeleteBuilder id(String id) {
        setId(id);
        return this;
    }

    public DeleteBuilder url(String urlString) {
        this.url = urlString;
        return this;
    }

    public DeleteBuilder header(String name, String value) {
        requestHeaders.put(name, value);
        return this;
    }

    public DeleteBuilder validate(LeafNodeBuilder validationBuilder) {
        validators.add(validationBuilder);
        return this;
    }

    public DeleteBuilder pipe(NodeBuilder builder) {
        pipes.add(builder);
        return this;
    }

    public DeleteBuilder returnVariables(String... variableKeys) {
        for (String variableKey : variableKeys) {
            returnVariables.add(variableKey);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    <R extends Base> R build(BuildContext buildContext) {
        List<Validator> validators = new ArrayList<>();

        // add default http status validator if unassigned
        if (this.validators.isEmpty()) {
            this.validate(status().success(200));
        }

        for (LeafNodeBuilder validatorBuilder : this.validators) {
            Validator validator = validatorBuilder.build(buildContext);
            validators.add(validator);
        }

        List<Node> stepNodeList = new ArrayList<>();
        for (NodeBuilder stepBuilder : pipes) {
            Node stepNode = stepBuilder.build(buildContext);
            stepNodeList.add(stepNode);
        }

        return (R) new DeleteNode(getId(), buildContext.getInstance(SpecificationBuilder.GLOBAL_CONFIGURATION), url, requestHeaders, validators, stepNodeList, returnVariables);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DeleteBuilder that = (DeleteBuilder) o;
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
        return "DeleteBuilder{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", requestHeaders=" + requestHeaders +
                ", validators=" + validators +
                ", pipes=" + pipes +
                ", returnVariables=" + returnVariables +
                '}';
    }

    static class DeleteNode extends OperationNode implements Delete {

        final String url;
        final Headers headers;
        final List<Node> pipes;
        final List<String> returnVariables;
        private final List<Validator> validateResponse;

        DeleteNode(String id, Configurations configurations, String url, Headers headers, List<Validator> validateResponse, List<Node> pipes, List<String> returnVariables) {
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
            DeleteNode deleteNode = (DeleteNode) o;
            return Objects.equals(url, deleteNode.url) &&
                    Objects.equals(headers, deleteNode.headers) &&
                    Objects.equals(validateResponse, deleteNode.validateResponse) &&
                    Objects.equals(pipes, deleteNode.pipes) &&
                    Objects.equals(returnVariables, deleteNode.returnVariables);
        }

        @Override
        public int hashCode() {
            return Objects.hash(url, headers, validateResponse, pipes, returnVariables);
        }

        @Override
        public String toString() {
            return "DeleteNode{" +
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
