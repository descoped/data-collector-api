package io.descoped.dc.api.content;

import java.util.Objects;

public class ContentStateKey {
    private final String topic;
    final String position;

    public ContentStateKey(String topic, String position) {
        this.topic = topic;
        this.position = position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContentStateKey that = (ContentStateKey) o;
        return topic.equals(that.topic) &&
                position.equals(that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topic, position);
    }

    @Override
    public String toString() {
        return "ContentStateKey{" +
                "topic='" + topic + '\'' +
                ", position='" + position + '\'' +
                '}';
    }
}
