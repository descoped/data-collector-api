package io.descoped.dc.server.db;


import io.descoped.dc.api.ids.TimeOrderedIds;
import java.util.UUID;
import java.nio.ByteBuffer;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SequenceKey implements Comparable<SequenceKey> {

    private final UUID id;
    private final String position;

    public SequenceKey(UUID id, String position) {
        this.id = id;
        this.position = position;
    }

    public UUID id() {
        return id;
    }

    public String position() {
        return position;
    }

    public static SequenceKey fromByteBuffer(ByteBuffer keyBuffer) {
        Objects.requireNonNull(keyBuffer);
        int positionLength = keyBuffer.get();
        byte[] positionBytes = new byte[positionLength];
        keyBuffer.get(positionBytes);
        long idMostSignificantBits = keyBuffer.getLong();
        long idLeastSignificantBits = keyBuffer.getLong();
        String position = new String(positionBytes, UTF_8);
        UUID id = new UUID(idMostSignificantBits, idLeastSignificantBits);
        return new SequenceKey(id, position);
    }

    public ByteBuffer toByteBuffer(ByteBuffer allocatedBuffer) {
        Objects.requireNonNull(allocatedBuffer);
        byte[] key = position.getBytes(UTF_8);
        allocatedBuffer.put((byte) key.length);
        allocatedBuffer.put(key);
        allocatedBuffer.putLong(id.getMostSignificantBits());
        allocatedBuffer.putLong(id.getLeastSignificantBits());
        return allocatedBuffer.flip();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SequenceKey that = (SequenceKey) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(position, that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, position);
    }

    @Override
    public int compareTo(SequenceKey that) {
        return TimeOrderedIds.ORDER.compare(this.id, that.id);
    }

    @Override
    public String toString() {
        return "SequenceKey{" +
                "id=" + id +
                ", position='" + position + '\'' +
                '}';
    }
}
