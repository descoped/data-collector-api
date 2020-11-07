package no.ssb.dc.api.security;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public interface BusinessSSLBundle {

    String bundleName();

    String getType();

    byte[] publicCertificate();

    byte[] privateCertificate();

    byte[] archiveCertificate();

    byte[] passphrase();

    default boolean isPEM() {
        Objects.requireNonNull(getType());
        return "pem".equalsIgnoreCase(getType());
    }

    static char[] byteArrayToChars(byte[] bytes) {
        CharBuffer cb = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(bytes));
        char[] chars = new char[cb.remaining()];
        cb.get(chars);
        return chars;
    }
}

