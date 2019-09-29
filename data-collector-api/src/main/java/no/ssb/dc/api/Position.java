package no.ssb.dc.api;

import java.util.Objects;

public class Position<P> implements Comparable<Position<?>> {
    private static final String UNDEFINED = "468f663c-2fed-44f8-9b77-04641a180f57";

    final P value;

    // TODO make private
    public Position(P value) {
        this.value = value;
    }

    public P value() {
        return value;
    }

    public Long asLong() {
        return (Long) value;
    }

    public boolean isUndefined() {
        return UNDEFINED.equals(value);
    }

    @Override
    public int compareTo(Position<?> thatPosition) {
        if (this.value instanceof Comparable) {
            Comparable thisPositionValue = (Comparable) this.value;
            Comparable thatPositionValue = (Comparable) thatPosition.value;
            return thisPositionValue.compareTo(thatPositionValue);
        }
        throw new UnsupportedOperationException("Position type: '" + this.value.getClass().getName() + "' does NOT implement Comparable");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position<?> position = (Position<?>) o;
        return Objects.equals(value, position.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "Position{" +
                "value=" + value +
                '}';
    }

    public String asString() {
        return value.toString();
    }

    public static Position<?> undefined() {
        return new Position<>(UNDEFINED);
    }

    public static <P> Position<P> noMoreElements() {
        return new Position<>(null);
    }

    public static <P> Position<P> create(P position) {
        return new Position<>(position);
    }


    public static <P> Position<P> nextPage(P nextPosition) {
        return new Position<>(nextPosition);
    }

}
