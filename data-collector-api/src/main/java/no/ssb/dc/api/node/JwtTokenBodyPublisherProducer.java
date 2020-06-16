package no.ssb.dc.api.node;

public interface JwtTokenBodyPublisherProducer extends IdentityTokenBodyPublisherProducer {

    Identity identity();

    String bindTo();

    String token();
}
