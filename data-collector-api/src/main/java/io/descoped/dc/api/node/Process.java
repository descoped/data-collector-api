package io.descoped.dc.api.node;

import io.descoped.dc.api.Processor;

import java.util.Set;

public interface Process extends Node {

    Class<? extends Processor> processorClass();

    Set<String> requiredOutputs();

}
