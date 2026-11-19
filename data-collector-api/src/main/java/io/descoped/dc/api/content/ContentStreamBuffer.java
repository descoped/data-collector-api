package io.descoped.dc.api.content;

import io.descoped.dc.api.ids.TimeOrderedIds;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface ContentStreamBuffer {

    /**
     * The message id: a UUIDv7, ordered by {@link TimeOrderedIds#ORDER}; null before the message is published.
     *
     * @return the id
     */
    UUID id();

    default long timestamp() {
        return TimeOrderedIds.timestamp(id());
    }

    String position();

    Set<String> keys();

    byte[] get(String contentKey);

    Map<String, byte[]> data();

    List<MetadataContent> manifest();

    interface Builder {

        Builder id(UUID id);

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
