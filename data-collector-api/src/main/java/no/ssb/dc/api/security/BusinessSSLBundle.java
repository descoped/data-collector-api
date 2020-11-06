package no.ssb.dc.api.security;

public interface BusinessSSLBundle {

    String bundleName();

    byte[] publicCertificate();

    byte[] privateCertificate();

    byte[] archiveCertificate();

    byte[] passphrase();

}
