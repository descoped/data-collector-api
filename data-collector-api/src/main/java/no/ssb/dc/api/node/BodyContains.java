package no.ssb.dc.api.node;

public interface BodyContains extends ResponsePredicate {

    Query getQuery();

    String getEqualToStringLiteral();

}
