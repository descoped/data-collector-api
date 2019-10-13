package no.ssb.dc.api.node;

import no.ssb.dc.api.http.Headers;

import java.util.List;

public interface Get extends Operation {

    String url();

    Headers headers();

    List<Validator> responseValidators();

    List<? extends Node> steps();

    List<String> returnVariables();

}
