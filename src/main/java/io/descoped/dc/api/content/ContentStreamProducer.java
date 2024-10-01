package io.descoped.dc.api.content;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public interface ContentStreamProducer extends AutoCloseable {

    ContentStreamBuffer.Builder builder();

    ContentStreamProducer copy(ContentStreamBuffer buffer);

    ContentStreamProducer produce(ContentStreamBuffer.Builder bufferBuilder);

    default void publishBuilders(ContentStreamBuffer.Builder... builders) throws ClosedContentStreamException {
        for (ContentStreamBuffer.Builder builder : builders) {
            produce(builder);
        }
        publish(Arrays.stream(builders).map(ContentStreamBuffer.Builder::position).collect(Collectors.toList()));
    }

    default void publish(List<String> positions) throws ClosedContentStreamException {
        publish(positions.toArray(new String[positions.size()]));
    }

    void publish(String... position);

}
