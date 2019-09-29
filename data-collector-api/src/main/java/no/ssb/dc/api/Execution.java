package no.ssb.dc.api;

import no.ssb.dc.api.context.ExecutionContext;

public interface Execution {

    ExecutionContext execute(ExecutionContext context);

}
