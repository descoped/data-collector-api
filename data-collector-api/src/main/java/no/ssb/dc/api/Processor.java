package no.ssb.dc.api;

import no.ssb.dc.api.context.ExecutionContext;

public interface Processor {

    ExecutionContext process(ExecutionContext input);

}
