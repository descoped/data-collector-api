package no.ssb.dc.api.security;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

public interface ProvidedBusinessSSLResource {

    String bundleName();

    String getType();

    char[] publicCertificate();

    char[] privateCertificate();

    byte[] archiveCertificate();

    char[] passphrase();

    default boolean isPEM() {
        Objects.requireNonNull(getType());
        return "pem".equalsIgnoreCase(getType());
    }

    /**
     * Convenience method that converts bytes to chars using UTF-8.
     *
     * Please notice: The input byte-array will be cleared after conversion.
     *
     * @param bytes input buffer
     * @return char-array as utf8 or empty char-array
     */
    static char[] safeConvertBytesToCharArrayAsUTF8(final byte[] bytes) {
        if (bytes == null) {
            return new char[0];
        }
        CharBuffer cb = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(bytes));
        final char[] chars = new char[cb.remaining()];
        cb.get(chars);
        Arrays.fill(bytes, (byte) 0);
        cb.clear();
        return chars;
    }
}
