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

    /**
     * The time of the message id.
     *
     * @return the milliseconds since 1970-01-01T00:00Z
     * @throws IllegalStateException if the buffer has no id yet (it is not published)
     */
    default long timestamp() {
        UUID id = id();
        if (id == null) {
            throw new IllegalStateException("No id before the message is published: " + position());
        }
        return TimeOrderedIds.timestamp(id);
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
