package io.descoped.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.descoped.dc.api.http.Headers;
import io.descoped.dc.api.node.Base;
import io.descoped.dc.api.node.BodyPublisher;
import io.descoped.dc.api.node.Configurations;
import io.descoped.dc.api.node.Node;
import io.descoped.dc.api.node.Post;
import io.descoped.dc.api.node.Validator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static io.descoped.dc.api.Builders.status;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class PostBuilder extends OperationBuilder {

    @JsonUnwrapped(prefix = "request")
    Headers requestHeaders = new Headers();
    @JsonProperty("bodyPublisher")
    BodyPublisherBuilder bodyPublisherBuilder;
    @JsonProperty("responseValidators")
    List<LeafNodeBuilder> validators = new ArrayList<>();
    @JsonProperty("pipes")
    List<NodeBuilder> pipes = new ArrayList<>();
    @JsonProperty
    List<String> returnVariables = new ArrayList<>();

    PostBuilder() {
        super(BuilderType.Post);
    }

    public PostBuilder(String id) {
        super(BuilderType.Post);
        setId(id);
    }

    public PostBuilder id(String id) {
        setId(id);
        return this;
    }

    public PostBuilder url(String urlString) {
        this.url = urlString;
        return this;
    }

    public PostBuilder header(String name, String value) {
        requestHeaders.put(name, value);
        return this;
    }

    public PostBuilder data(BodyPublisherBuilder bodyPublisherBuilder) {
        this.bodyPublisherBuilder = bodyPublisherBuilder;
        return this;
    }

    public PostBuilder validate(LeafNodeBuilder validationBuilder) {
        validators.add(validationBuilder);
        return this;
    }

    public PostBuilder pipe(NodeBuilder builder) {
        pipes.add(builder);
        return this;
    }

    public PostBuilder returnVariables(String... variableKeys) {
        for (String variableKey : variableKeys) {
            returnVariables.add(variableKey);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    <R extends Base> R build(BuildContext buildContext) {
        BodyPublisher bodyPublisher = bodyPublisherBuilder == null ? null : bodyPublisherBuilder.build(buildContext);

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

        return (R) new PostBuilder.PostNode(getId(), buildContext.getInstance(SpecificationBuilder.GLOBAL_CONFIGURATION), url, requestHeaders, bodyPublisher, validators, stepNodeList, returnVariables);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PostBuilder that = (PostBuilder) o;
        return Objects.equals(requestHeaders, that.requestHeaders) &&
                Objects.equals(bodyPublisherBuilder, that.bodyPublisherBuilder) &&
                Objects.equals(validators, that.validators) &&
                Objects.equals(pipes, that.pipes) &&
                Objects.equals(returnVariables, that.returnVariables);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), requestHeaders, bodyPublisherBuilder, validators, pipes, returnVariables);
    }

    @Override
    public String toString() {
        return "PostBuilder{" +
                "requestHeaders=" + requestHeaders +
                ", bodyPublisherBuilder=" + bodyPublisherBuilder +
                ", validators=" + validators +
                ", pipes=" + pipes +
                ", returnVariables=" + returnVariables +
                '}';
    }

    static class PostNode extends OperationNode implements Post {

        final String url;
        final Headers headers;
        final List<Node> pipes;
        final List<String> returnVariables;
        final BodyPublisher bodyPublisher;
        final List<Validator> validateResponse;

        PostNode(String id, Configurations configurations, String url, Headers headers, BodyPublisher bodyPublisher, List<Validator> validateResponse, List<Node> pipes, List<String> returnVariables) {
            super(configurations, id);
            this.url = url;
            this.headers = headers;
            this.bodyPublisher = bodyPublisher;
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
        public BodyPublisher bodyPublisher() {
            return bodyPublisher;
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
            PostNode postNode = (PostNode) o;
            return Objects.equals(url, postNode.url) &&
                    Objects.equals(headers, postNode.headers) &&
                    Objects.equals(pipes, postNode.pipes) &&
                    Objects.equals(returnVariables, postNode.returnVariables) &&
                    Objects.equals(bodyPublisher, postNode.bodyPublisher) &&
                    Objects.equals(validateResponse, postNode.validateResponse);
        }

        @Override
        public int hashCode() {
            return Objects.hash(url, headers, pipes, returnVariables, bodyPublisher, validateResponse);
        }

        @Override
        public String toString() {
            return "PostNode{" +
                    "url='" + url + '\'' +
                    ", headers=" + headers +
                    ", pipes=" + pipes +
                    ", returnVariables=" + returnVariables +
                    ", bodyPublisher=" + bodyPublisher +
                    ", validateResponse=" + validateResponse +
                    '}';
        }
    }
}
