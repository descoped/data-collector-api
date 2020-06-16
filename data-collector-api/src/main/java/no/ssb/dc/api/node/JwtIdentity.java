package no.ssb.dc.api.node;

import no.ssb.dc.api.node.builder.JwtClaims;
import no.ssb.dc.api.node.builder.JwtHeaderClaims;

public interface JwtIdentity extends Identity {

    JwtHeaderClaims headerClaims();

    JwtClaims claims();

}
