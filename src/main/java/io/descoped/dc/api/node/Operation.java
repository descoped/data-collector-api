package io.descoped.dc.api.node;

import io.descoped.dc.api.http.Headers;

import java.util.List;

// common interface for http operations
public interface Operation extends NodeWithId {

    String url();

    Headers headers();

    List<Validator> responseValidators();

    List<? extends Node> steps();

    List<String> returnVariables();

}
