package no.ssb.dc.api.node;

import no.ssb.dc.api.http.HttpStatusCode;
import no.ssb.dc.api.node.builder.ResponsePredicateBuilder;

import java.util.List;
import java.util.Map;

public interface HttpStatusValidation extends Validator {

    Map<HttpStatusCode, List<ResponsePredicateBuilder>> success();

    List<HttpStatusCode> failed();

}
