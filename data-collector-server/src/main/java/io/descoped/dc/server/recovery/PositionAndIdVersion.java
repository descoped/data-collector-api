package io.descoped.dc.server.recovery;


import io.descoped.dc.api.ids.TimeOrderedIds;
import java.util.UUID;
import java.util.Objects;

/**
 * Maintain the state of the oldest version of an id
 */
public class PositionAndIdVersion {

    private String position;
    private UUID id;

    public PositionAndIdVersion() {
    }

    public String position() {
        return position;
    }

    public UUID id() {
        return id;
    }

    public boolean isEmpty() {
        return this.id == null && this.position == null;
    }

    public boolean isOlder(UUID id) {
        if (isEmpty()) {
            return true; // ensure we accept
        }
        return TimeOrderedIds.ORDER.compare(this.id, id) > 0;
    }

    public boolean compareAndSet(UUID id, String position) {
        if (isOlder(id)) {
            this.id = id;
            this.position = position;
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PositionAndIdVersion that = (PositionAndIdVersion) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(position, that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, position);
    }

    @Override
    public String toString() {
        return "PositionAndIdVersion{" +
                "id=" + id +
                ", position='" + position + '\'' +
                '}';
    }
}
