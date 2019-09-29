package no.ssb.dc.api.content;

import java.util.Objects;

public class ContentStateKey {
    private final String namespace;
    final String position;

    public ContentStateKey(String namespace, String position) {
        this.namespace = namespace;
        this.position = position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContentStateKey that = (ContentStateKey) o;
        return namespace.equals(that.namespace) &&
                position.equals(that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, position);
    }

    @Override
    public String toString() {
        return "ContentStateKey{" +
                "namespace='" + namespace + '\'' +
                ", position='" + position + '\'' +
                '}';
    }
}
