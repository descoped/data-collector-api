package io.descoped.dc.api.security;

import java.util.Objects;

public interface BusinessSSLResource extends AutoCloseable {

    String bundleName();

    String getType();

    char[] publicCertificate();

    char[] privateCertificate();

    byte[] archiveCertificate();

    char[] passphrase();

    @Override
    void close();

    default boolean isPEM() {
        Objects.requireNonNull(getType());
        return "pem".equalsIgnoreCase(getType());
    }
}
