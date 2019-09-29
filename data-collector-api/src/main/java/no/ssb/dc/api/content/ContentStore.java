package no.ssb.dc.api.content;

import no.ssb.dc.api.http.Metadata;

import java.util.Set;

public interface ContentStore {

    String lastPosition(String namespace);

    Set<String> contentKeys(String namespace, String position);

    void addPaginationDocument(String namespace, String contentKey, byte[] content, Metadata metadata);

    void bufferPaginationEntryDocument(String namespace, String position, String contentKey, byte[] content, Metadata metadata);

    void bufferDocument(String namespace, String position, String contentKey, byte[] content, Metadata metadata);

    void publish(String namespace, String... position);

}
