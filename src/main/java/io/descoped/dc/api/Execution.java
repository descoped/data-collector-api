package io.descoped.dc.api;

import io.descoped.dc.api.context.ExecutionContext;

public interface Execution {

    ExecutionContext execute(ExecutionContext context);

}
