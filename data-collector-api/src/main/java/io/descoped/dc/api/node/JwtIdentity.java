package io.descoped.dc.api.node;

import io.descoped.dc.api.node.builder.JwtClaims;
import io.descoped.dc.api.node.builder.JwtHeaderClaims;

public interface JwtIdentity extends Identity {

    JwtHeaderClaims headerClaims();

    JwtClaims claims();

}
