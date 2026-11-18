package io.descoped.dc.api.content;

public interface ContentStream extends AutoCloseable {

    String lastPosition(String topic);

    ContentStreamBuffer lastMessage(String topic);

    ContentStreamProducer producer(String topic);

    default ContentStreamConsumer consumer(String topic) {
        return consumer(topic, null);
    }

    ContentStreamConsumer consumer(String topic, ContentStreamCursor cursor);

    void closeAndRemoveProducer(String topic);

    void closeAndRemoveConsumer(String topic);
}
