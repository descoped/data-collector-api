package io.descoped.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.descoped.dc.api.ConfigSetter;
import io.descoped.dc.api.context.ExecutionContext;
import io.descoped.dc.api.http.Headers;
import io.descoped.dc.api.node.Base;
import io.descoped.dc.api.node.FlowContext;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class SpecificationContextBuilder extends ConfigurationBuilder {

    Headers headers = new Headers();
    @JsonProperty
    Map<String, Object> variables = new LinkedHashMap<>();
    @JsonProperty
    Map<Object, Object> globalState = new LinkedHashMap<>();

    public SpecificationContextBuilder() {
        super(BuilderType.SpecificationContext);
    }

    @JsonProperty("headers")
    public Map<String, List<String>> headerMap() {
        return headers.asMap();
    }

    @ConfigSetter
    public SpecificationContextBuilder topic(String topicName) {
        globalState("global.topic", topicName);
        return this;
    }

    @ConfigSetter
    public SpecificationContextBuilder header(String name, String value) {
        headers.put(name, value);
        return this;
    }

    @ConfigSetter
    public SpecificationContextBuilder variable(String name, Object value) {
        variables.put(name, value);
        return this;
    }

    @ConfigSetter
    public SpecificationContextBuilder globalState(Object key, Object value) {
        globalState.put(key, value);
        return this;
    }

    public Object globalState(Object key) {
        return globalState.get(key);
    }

    /*
     * TODO the ExecutionContext.join() and overlays of context state must be improved. Possible race conditions here.
     * see FlowBuilder.end() and NodeBuilder.build().
     */
    @Override
    public <R extends Base> R build(BuildContext buildContext) {
        ExecutionContext context = ExecutionContext.empty();
        // do not set set if headers is empty
        if (!headers.asMap().isEmpty()) {
            context.state(Headers.class, headers);
        }
        context.variables().putAll(variables);
        globalState.forEach(context::globalState);
        return (R) new FlowContextNode(context);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SpecificationContextBuilder that = (SpecificationContextBuilder) o;
        return Objects.equals(headers, that.headers) &&
                Objects.equals(variables, that.variables) &&
                Objects.equals(globalState, that.globalState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), headers, variables, globalState);
    }

    @Override
    public String toString() {
        return "SpecificationContextBuilder{" +
                "headers=" + headers +
                ", variables=" + variables +
                ", globalState=" + globalState +
                '}';
    }

    static class FlowContextNode extends LeafNode implements FlowContext {

        private final ExecutionContext context;

        FlowContextNode(ExecutionContext context) {
            this.context = context;
        }

        public String topic() {
            String topic = context.state("global.topic");
            if (topic == null) {
                throw new RuntimeException("Topic is NOT configured!");
            }
            return topic;
        }

        @Override
        public ExecutionContext globalContext() {
            return context;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FlowContextNode that = (FlowContextNode) o;
            return Objects.equals(context, that.context);
        }

        @Override
        public int hashCode() {
            return Objects.hash(context);
        }

        @Override
        public String toString() {
            return "FlowContextNode{" +
                    "context=" + context +
                    '}';
        }
    }
}
