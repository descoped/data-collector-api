package no.ssb.dc.api.content;

import java.util.Set;

public interface ContentStore extends AutoCloseable {

    void lock(String topic);

    void unlock(String topic);

    String lastPosition(String topic);

    Set<String> contentKeys(String topic, String position);

    void addPaginationDocument(String topic, String contentKey, byte[] content, HttpRequestInfo httpRequestInfo);

    void bufferPaginationEntryDocument(String topic, String position, String contentKey, byte[] content, HttpRequestInfo httpRequestInfo);

    void bufferDocument(String topic, String position, String contentKey, byte[] content, HttpRequestInfo httpRequestInfo);

    void publish(String topic, String... position);

    HealthContentStreamMonitor monitor();

    boolean isClosed();

}
