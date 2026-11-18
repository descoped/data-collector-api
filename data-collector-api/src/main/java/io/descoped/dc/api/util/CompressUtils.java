package io.descoped.dc.api.util;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CompressUtils {

    private static final int BUFFER_SIZE = 4 * 1024;

    public static boolean isGzipCompressed(Path file) {
        try {
            try (FileInputStream fis = new FileInputStream(file.toFile())) {
                byte[] buf = new byte[4];
                if (fis.read(buf) != -1) {
                    return isGzipCompressed(buf);
                }
                return false;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isGzipCompressed(final byte[] compressed) {
        return (compressed[0] == (byte) (GZIPInputStream.GZIP_MAGIC)) && (compressed[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8));
    }

    public static <R extends OutputStream> R gzip(byte[] data, R outputStream) {
        return gzip(new ByteArrayInputStream(data), outputStream);
    }

    public static <R extends OutputStream> R gzip(Path file, R outputStream) {
        try {
            return gzip(new FileInputStream(file.toFile()), outputStream);

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static <R extends OutputStream> R gzip(InputStream inputStream, R outputStream) {
        try (inputStream) {
            try (outputStream) {
                try (GZIPOutputStream gos = new GZIPOutputStream(outputStream)) {
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
                        gos.write(buffer, 0, len);
                    }
                }
                return outputStream;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <R extends OutputStream> R gunzip(byte[] data, R outputStream) {
        return gunzip(new ByteArrayInputStream(data), outputStream);
    }

    public static <R extends OutputStream> R gunzip(Path file, R outputStream) {
        try {
            return gunzip(new FileInputStream(file.toFile()), outputStream);

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static <R extends OutputStream> R gunzip(InputStream inputStream, R outputStream) {
        try (inputStream) {
            try (GZIPInputStream gis = new GZIPInputStream(inputStream)) {
                try (outputStream) {
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int len;
                    while ((len = gis.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, len);
                    }
                }
                return outputStream;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
