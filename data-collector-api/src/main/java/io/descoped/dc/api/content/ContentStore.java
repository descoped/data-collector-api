package io.descoped.dc.api.content;

import java.util.Map;
import java.util.Set;

public interface ContentStore extends AutoCloseable {

    void lock(String topic);

    void unlock(String topic);

    ContentStream contentStream();

    String lastPosition(String topic);

    Set<String> contentKeys(String topic, String position);

    void addPaginationDocument(String topic, String position, String contentKey, byte[] content, HttpRequestInfo httpRequestInfo);

    void bufferPaginationEntryDocument(String topic, String position, String contentKey, byte[] content, HttpRequestInfo httpRequestInfo, Map<String, Object> state);

    void bufferDocument(String topic, String position, String contentKey, byte[] content, HttpRequestInfo httpRequestInfo);

    void publish(String topic, String... position);

    HealthContentStreamMonitor monitor();

    void closeTopic(String topic);

    boolean isClosed();

}
