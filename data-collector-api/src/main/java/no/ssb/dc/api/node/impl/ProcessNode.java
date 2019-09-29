package no.ssb.dc.api.node.impl;

import no.ssb.dc.api.Processor;
import no.ssb.dc.api.node.Node;
import no.ssb.dc.api.node.Process;

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

public class ProcessNode extends FlowNode implements Process {

    final Class<? extends Processor> processorClass;
    final Set<String> requiredOutputs;

    public ProcessNode(Class<? extends Processor> processorClass, Set<String> requiredOutputs) {
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
        return Objects.equals(processorClass, that.processorClass) &&
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
