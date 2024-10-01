package io.descoped.dc.api.content;

import de.huxhorn.sulky.ulid.ULID;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ContentStreamBuffer {

    ULID.Value ulid();

    default long timestamp() {
        return ulid().timestamp();
    }

    String position();

    Set<String> keys();

    byte[] get(String contentKey);

    Map<String, byte[]> data();

    List<MetadataContent> manifest();

    interface Builder {

        Builder ulid(ULID.Value ulid);

        Builder position(String position);

        String position();

        ContentStreamBuffer.Builder put(String key, byte[] payload);

        Builder buffer(String contentKey, byte[] content, MetadataContent manifest);

        byte[] get(String contentKey);

        Set<String> keys();

        List<MetadataContent> manifest();

        ContentStreamBuffer build();
    }

}
