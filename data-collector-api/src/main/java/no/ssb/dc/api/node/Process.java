package no.ssb.dc.api.node;

import no.ssb.dc.api.Processor;

import java.util.Set;

public interface Process extends Node {

    Class<? extends Processor> processorClass();

    Set<String> requiredOutputs();

}
