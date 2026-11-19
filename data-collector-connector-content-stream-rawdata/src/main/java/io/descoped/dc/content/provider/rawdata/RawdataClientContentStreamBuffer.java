package io.descoped.dc.content.provider.rawdata;

import io.descoped.dc.api.content.ContentStreamBuffer;
import io.descoped.dc.api.content.MetadataContent;
import io.descoped.rawdata.api.RawdataMessage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RawdataClientContentStreamBuffer implements ContentStreamBuffer {

    private final UUID id;
    private final String position;
    private final Map<String, byte[]> data;
    private final List<MetadataContent> manifestList;

    RawdataClientContentStreamBuffer(UUID id, String position, Map<String, byte[]> data, List<MetadataContent> manifestList) {
        this.id = id;
        this.position = position;
        this.data = data;
        this.manifestList = manifestList;
    }

    public static ContentStreamBuffer of(RawdataMessage message) {
        if (message == null) {
            return null;
        }
        return new RawdataClientContentStreamBuffer(message.id(), message.position(), message.data(), new ArrayList<>());
    }

    @Override
    public UUID id() {
        return id;
    }

    @Override
    public String position() {
        return position;
    }

    @Override
    public Set<String> keys() {
        return data.keySet();
    }

    @Override
    public byte[] get(String contentKey) {
        return data.get(contentKey);
    }

    @Override
    public Map<String, byte[]> data() {
        return data;
    }

    @Override
    public List<MetadataContent> manifest() {
        return manifestList;
    }

    public static class Builder implements ContentStreamBuffer.Builder {

        private UUID id;
        private String position;
        final Map<String, byte[]> data = new LinkedHashMap<>();
        private List<MetadataContent> manifestList = new ArrayList<>();

        @Override
        public ContentStreamBuffer.Builder id(UUID id) {
            this.id = id;
            return this;
        }

        @Override
        public ContentStreamBuffer.Builder position(String position) {
            this.position = position;
            return this;
        }

        @Override
        public String position() {
            return position;
        }

        @Override
        public ContentStreamBuffer.Builder put(String key, byte[] payload) {
            data.put(key, payload);
            return this;
        }

        @Override
        public Builder buffer(String contentKey, byte[] content, MetadataContent manifest) {
            data.put(contentKey, content);
            // do not add manifest entry
            if (manifest != null) {
                manifestList.add(manifest);
            }
            return this;
        }

        @Override
        public byte[] get(String contentKey) {
            return data.get(contentKey);
        }

        @Override
        public Set<String> keys() {
            return data.keySet();
        }

        @Override
        public List<MetadataContent> manifest() {
            return manifestList;
        }

        @Override
        public ContentStreamBuffer build() {
            return new RawdataClientContentStreamBuffer(id, position, data, manifestList);
        }

    }
}
