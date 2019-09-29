package no.ssb.dc.api.content;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ContentStreamBuffer {

    String position();

    Map<String,byte[]> data();

    List<MetadataContent> manifest();

    interface Builder {
        Builder position(String position);

        Builder buffer(String contentKey, byte[] content, MetadataContent manifest);

        Set<String> keys();

        List<MetadataContent> manifest();

        ContentStreamBuffer build();
    }

}
