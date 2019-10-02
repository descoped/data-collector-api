package no.ssb.dc.api.node;

import no.ssb.dc.api.http.HttpStatusCode;

import java.util.List;

public interface ValidateResponse extends Base {

    List<HttpStatusCode> success();

    List<HttpStatusCode> failed();
}
