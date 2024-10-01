package io.descoped.dc.api.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CryptoUtils {

    public static ByteBuffer makeMD5Hash(ByteBuffer buf) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(buf);
            byte[] digest = md.digest();
            ByteBuffer md5buf = ByteBuffer.allocate(2 * digest.length);
            for (byte bb : digest) {
                md5buf.put(String.format("%02x", bb).getBytes(StandardCharsets.UTF_8));
            }
            md5buf.flip();
            return md5buf;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
