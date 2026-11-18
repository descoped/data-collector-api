package io.descoped.dc.api;

import io.descoped.dc.api.context.ExecutionContext;

public interface Processor {

    ExecutionContext process(ExecutionContext input);

}
