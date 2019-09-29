package no.ssb.dc.api.content;

import java.util.Set;

public interface ContentStore {

    String lastPosition(String namespace);

    Set<String> contentKeys(String namespace, String position);

    void addPaginationDocument(String namespace, String contentKey, byte[] content, HttpRequestInfo httpRequestInfo);

    void bufferPaginationEntryDocument(String namespace, String position, String contentKey, byte[] content, HttpRequestInfo httpRequestInfo);

    void bufferDocument(String namespace, String position, String contentKey, byte[] content, HttpRequestInfo httpRequestInfo);

    void publish(String namespace, String... position);

}
