package no.ssb.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import no.ssb.dc.api.node.Base;
import no.ssb.dc.api.node.BodyPart;
import no.ssb.dc.api.node.BodyPublisher;
import no.ssb.dc.api.node.FormEncoding;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class BodyPublisherBuilder extends OperationPublisherBuilder {

    @JsonIgnore FormEncoding encoding;
    @JsonProperty("plainTextData") String plainText;
    @JsonProperty("urlEncodedData") String urlEncodedData;
    @JsonProperty("partsData") List<BodyPart> parts = new ArrayList<>();

    public BodyPublisherBuilder() {
        super(BuilderType.BodyPublisher);
    }

    public BodyPublisherBuilder plainText(String text) {
        this.plainText = text;
        this.encoding = FormEncoding.TEXT_PLAIN;
        return this;
    }

    public BodyPublisherBuilder urlEncodedData(String data) {
        this.urlEncodedData = data;
        this.encoding = FormEncoding.APPLICATION_X_WWW_FORM_URLENCODED;
        return this;
    }

    public BodyPublisherBuilder textPart(String name, String value) {
        this.encoding = FormEncoding.MULTIPART_FORM_DATA;
        this.parts.add(new BodyPart(name, value, StandardCharsets.UTF_8));
        return this;
    }

    public BodyPublisherBuilder formPart(String name, String filename, byte[] value) {
        this.encoding = FormEncoding.MULTIPART_FORM_DATA;
        this.parts.add(new BodyPart(name, filename, value, StandardCharsets.UTF_8));
        return this;
    }

    public BodyPublisherBuilder formPart(String name, String filename, String value) {
        this.encoding = FormEncoding.MULTIPART_FORM_DATA;
        this.parts.add(new BodyPart(name, filename, value, StandardCharsets.UTF_8));
        return this;
    }

    @Override
    public <R extends Base> R build(BuildContext buildContext) {
        return (R) new BodyPublisherNode(encoding, plainText, urlEncodedData, parts);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BodyPublisherBuilder that = (BodyPublisherBuilder) o;
        return encoding == that.encoding &&
                Objects.equals(plainText, that.plainText) &&
                Objects.equals(urlEncodedData, that.urlEncodedData) &&
                Objects.equals(parts, that.parts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), encoding, plainText, urlEncodedData, parts);
    }

    @Override
    public String toString() {
        return "BodyPublisherBuilder{" +
                "encoding=" + encoding +
                ", plainText='" + plainText + '\'' +
                ", urlEncodedData='" + urlEncodedData + '\'' +
                ", parts=" + parts +
                '}';
    }

    static class BodyPublisherNode extends LeafNode implements BodyPublisher {

        private final FormEncoding encoding;
        private final String plainText;
        private final String urlEncodedData;
        private final List<BodyPart> parts;

        public BodyPublisherNode(FormEncoding encoding, String plainText, String urlEncodedData, List<BodyPart> parts) {
            this.encoding = encoding;
            this.plainText = plainText;
            this.urlEncodedData = urlEncodedData;
            this.parts = parts;
        }

        @Override
        public FormEncoding getEncoding() {
            return encoding;
        }

        @Override
        public String getPlainText() {
            return plainText;
        }

        @Override
        public String getUrlEncodedData() {
            return urlEncodedData;
        }

        @Override
        public List<BodyPart> getParts() {
            return parts;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BodyPublisherNode that = (BodyPublisherNode) o;
            return encoding == that.encoding &&
                    Objects.equals(plainText, that.plainText) &&
                    Objects.equals(urlEncodedData, that.urlEncodedData) &&
                    Objects.equals(parts, that.parts);
        }

        @Override
        public int hashCode() {
            return Objects.hash(encoding, plainText, urlEncodedData, parts);
        }

        @Override
        public String toString() {
            return "BodyPublisherNode{" +
                    "encoding=" + encoding +
                    ", plainText='" + plainText + '\'' +
                    ", urlEncodedData='" + urlEncodedData + '\'' +
                    ", parts=" + parts +
                    '}';
        }
    }

}
