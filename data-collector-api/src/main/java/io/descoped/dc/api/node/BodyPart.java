package io.descoped.dc.api.node;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.nio.charset.Charset;
import java.util.Objects;

public class BodyPart {

    public final String name;
    public final String filename;
    public final Object value;
    public final Charset charset;

    public BodyPart(String name, Object value, Charset charset) {
        this.name = name;
        this.filename = null;
        this.value = value;
        this.charset = charset;
    }

    public BodyPart(String name, String filename, Object value, Charset charset) {
        this.name = name;
        this.filename = filename;
        this.value = value;
        this.charset = charset;
    }

    @JsonIgnore
    public boolean isTextPart() {
        return filename == null;
    }

    @JsonIgnore
    public boolean isFormPart() {
        return filename != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BodyPart part = (BodyPart) o;
        return Objects.equals(name, part.name) &&
                Objects.equals(filename, part.filename) &&
                Objects.equals(value, part.value) &&
                Objects.equals(charset, part.charset);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, filename, value, charset);
    }

    @Override
    public String toString() {
        return "BodyPart{" +
                "name='" + name + '\'' +
                ", filename='" + filename + '\'' +
                ", value=" + value +
                ", charset=" + charset +
                '}';
    }
}
