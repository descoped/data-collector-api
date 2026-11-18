package io.descoped.dc.api.node;

import io.descoped.dc.api.context.ExecutionContext;

public interface FlowContext extends Configuration {

    String topic();

    ExecutionContext globalContext();

}
