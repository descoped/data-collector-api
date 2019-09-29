package no.ssb.dc.api.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import no.ssb.dc.api.BuilderType;
import no.ssb.dc.api.NodeBuilderDeserializer;
import no.ssb.dc.api.Processor;
import no.ssb.dc.api.node.BaseNode;
import no.ssb.dc.api.node.impl.ProcessNode;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class ProcessBuilder extends NodeBuilder {

    @JsonProperty Class<? extends Processor> processorClass;
    @JsonProperty Set<String> requiredOutputs = new LinkedHashSet<>();

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
    <R extends BaseNode> R build(Map<String, NodeBuilder> nodeBuilderById, Map<String, R> nodeInstanceById) {
        return (R) new ProcessNode(processorClass, requiredOutputs);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProcessBuilder)) return false;
        ProcessBuilder that = (ProcessBuilder) o;
        return Objects.equals(processorClass, that.processorClass) &&
                Objects.equals(requiredOutputs, that.requiredOutputs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(processorClass, requiredOutputs);
    }

    @Override
    public String toString() {
        return "ProcessBuilder{" +
                "processorClass=" + processorClass +
                ", requiredOutputs=" + requiredOutputs +
                '}';
    }
}
