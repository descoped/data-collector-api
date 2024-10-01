package io.descoped.dc.api.context;

import io.descoped.dc.api.services.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static java.util.Optional.ofNullable;

public class ExecutionContext {

    private final static Logger LOG = LoggerFactory.getLogger(ExecutionContext.class);

    final Services services;
    final Map<Object, Object> globalState;
    final Map<String, Object> variables;
    final Map<Object, Object> state;

    private ExecutionContext(Services services, Map<Object, Object> globalState, Map<String, Object> variables, Map<Object, Object> state) {
        this.globalState = globalState;
        this.services = services;
        this.variables = variables;
        this.state = state;
    }

    public static ExecutionContext empty() {
        return new Builder().build();
    }

    public static ExecutionContext of(ExecutionContext input) {
        return new Builder().of(input).build();
    }

    public ExecutionContext join(ExecutionContext anotherContext) {
        ExecutionContext copyOfThis = of(this);
        ExecutionContext accumulated = of(anotherContext);
        accumulated.variables.putAll(copyOfThis.variables);
        accumulated.globalState.putAll(copyOfThis.globalState);
        accumulated.state.putAll(copyOfThis.state);
        this.variables.putAll(accumulated.variables);
        this.globalState.putAll(accumulated.globalState);
        this.state.putAll(accumulated.state);
        return this;
    }

    public Services services() {
        Objects.requireNonNull(services);
        return services;
    }

    public ExecutionContext globalState(Object key, Object value) {
        globalState.put(key, value);
        return this;
    }

    public <R> R globalStateIfAbsent(Object key, Supplier<R> supplier) {
        return (R) globalState.putIfAbsent(key, supplier);
    }

    public Map<String, Object> variables() {
        return variables;
    }

    public Object variable(String key) {
        return variables.get(key);
    }

    public ExecutionContext variable(String key, Object value) {
        variables.put(key, value);
        return this;
    }

    public ExecutionContext state(Object key, Object value) {
        state.put(key, value);
        return this;
    }

    public <R> R state(Class<R> key) {
        return (R) state((Object) key);
    }

    public <R> R state(Object key) {
        R r = (R) state.get(key);
        if (r == null) {
            r = (R) globalState.get(key);
        }
        return r;
    }

    public void releaseState(Object key) {
        if (state.remove(key) == null) {
            LOG.error("unable to release state key={}", key);
        }
    }

    public ExecutionContext merge(ExecutionContext context) {
        this.state.putAll(context.state);
        this.variables.putAll(context.variables);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExecutionContext context = (ExecutionContext) o;
        return Objects.equals(globalState, context.globalState) &&
                Objects.equals(variables, context.variables) &&
                Objects.equals(state, context.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(globalState, variables, state);
    }

    @Override
    public String toString() {
        return "ExecutionInput{" +
                "globalState=" + globalState +
                ", state=" + state +
                ", variables=" + variables +
                '}';
    }

    public static class Builder {

        private Services services;
        private Map<Object, Object> globalState;
        private Map<String, Object> variables;
        private Map<Object, Object> state;

        public Builder services(Services services) {
            this.services = services;
            return this;
        }

        public Builder services(ExecutionContext input) {
            this.services = input.services;
            return this;
        }

        public Builder globalState(Map<Object, Object> globalState) {
            this.globalState = globalState;
            return this;
        }

        public Builder globalState(ExecutionContext input) {
            this.globalState = input.globalState;
            return this;
        }

        public Builder variables(Map<String, Object> variables) {
            this.variables = variables;
            return this;
        }

        public Builder variables(ExecutionContext input) {
            this.variables = input.variables;
            return this;
        }

        public Builder state(Map<Object, Object> state) {
            this.state = state;
            return this;
        }

        public Builder state(ExecutionContext input) {
            this.state = input.state;
            return this;
        }

        public Builder of(ExecutionContext input) {
            this.services = input.services;
            this.globalState = input.globalState;
            if (this.variables == null) {
                this.variables = new LinkedHashMap<>();
            }
            this.variables.putAll(input.variables);
            if (this.state == null) {
                this.state = new LinkedHashMap<>();
            }
            this.state.putAll(input.state);
            return this;
        }

        public ExecutionContext build() {
            return new ExecutionContext(
                    ofNullable(services).orElseGet(Services::create),
                    ofNullable(globalState).orElseGet(ConcurrentHashMap::new),
                    ofNullable(variables).orElseGet(LinkedHashMap::new),
                    ofNullable(state).orElseGet(LinkedHashMap::new)
            );
        }
    }
}
