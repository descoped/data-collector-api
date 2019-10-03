package no.ssb.dc.api.node;

import no.ssb.dc.api.context.ExecutionContext;

public interface FlowContext extends Configuration {

    ExecutionContext globalContext();

}
