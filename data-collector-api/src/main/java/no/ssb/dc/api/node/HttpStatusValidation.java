package no.ssb.dc.api.node;

import no.ssb.dc.api.http.HttpStatusCode;

import java.util.List;

public interface HttpStatusValidation extends Validator {

    List<HttpStatusCode> success();

    List<HttpStatusCode> failed();

}
