package io.descoped.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.descoped.dc.api.Processor;
import io.descoped.dc.api.node.Base;
import io.descoped.dc.api.node.Configurations;
import io.descoped.dc.api.node.Node;
import io.descoped.dc.api.node.Process;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class ProcessBuilder extends NodeBuilder {

    @JsonProperty
    Class<? extends Processor> processorClass;
    @JsonProperty
    Set<String> requiredOutputs = new LinkedHashSet<>();

    public ProcessBuilder(Class<? extends Processor> processorClass) {
        super(BuilderType.Process);
        this.processorClass = processorClass;
    }

    public ProcessBuilder output(String variable) {
        requiredOutputs.add(variable);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    <R extends Base> R build(BuildContext buildContext) {
        return (R) new ProcessNode(buildContext.getInstance(SpecificationBuilder.GLOBAL_CONFIGURATION), processorClass, requiredOutputs);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ProcessBuilder that = (ProcessBuilder) o;
        return processorClass.equals(that.processorClass) &&
                Objects.equals(requiredOutputs, that.requiredOutputs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), processorClass, requiredOutputs);
    }

    @Override
    public String toString() {
        return "ProcessBuilder{" +
                "processorClass=" + processorClass +
                ", requiredOutputs=" + requiredOutputs +
                '}';
    }

    static class ProcessNode extends FlowNode implements Process {

        final Class<? extends Processor> processorClass;
        final Set<String> requiredOutputs;

        ProcessNode(Configurations configurations, Class<? extends Processor> processorClass, Set<String> requiredOutputs) {
            super(configurations);
            this.processorClass = processorClass;
            this.requiredOutputs = requiredOutputs;
        }

        @Override
        public Class<? extends Processor> processorClass() {
            return processorClass;
        }

        @Override
        public Set<String> requiredOutputs() {
            return requiredOutputs;
        }

        @Override
        public Iterator<? extends Node> iterator() {
            return createNodeList().iterator();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ProcessNode that = (ProcessNode) o;
            return processorClass.equals(that.processorClass) &&
                    Objects.equals(requiredOutputs, that.requiredOutputs);
        }

        @Override
        public int hashCode() {
            return Objects.hash(processorClass, requiredOutputs);
        }

        @Override
        public String toString() {
            return "ProcessNode{" +
                    "processorClass=" + processorClass +
                    ", requiredOutputs=" + requiredOutputs +
                    '}';
        }
    }

}
