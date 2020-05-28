package no.ssb.dc.api.node;

import no.ssb.dc.api.http.HttpStatus;

import java.util.List;
import java.util.Map;

public interface HttpStatusValidation extends Validator {

    Map<HttpStatus, List<ResponsePredicate>> success();

    List<HttpStatus> failed();

}
