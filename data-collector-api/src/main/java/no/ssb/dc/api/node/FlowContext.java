package no.ssb.dc.api.node;

import no.ssb.dc.api.context.ExecutionContext;

public interface FlowContext extends Configuration {

    String topic();

    ExecutionContext globalContext();

}
