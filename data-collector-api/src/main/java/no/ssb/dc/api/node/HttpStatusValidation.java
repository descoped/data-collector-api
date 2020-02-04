package no.ssb.dc.api.node;

import no.ssb.dc.api.http.HttpStatusCode;

import java.util.List;
import java.util.Map;

public interface HttpStatusValidation extends Validator {

    Map<HttpStatusCode, List<ResponsePredicate>> success();

    List<HttpStatusCode> failed();

}
