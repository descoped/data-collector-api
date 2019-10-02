package no.ssb.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import no.ssb.dc.api.http.HttpStatusCode;
import no.ssb.dc.api.node.Base;
import no.ssb.dc.api.node.HttpStatusValidation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class HttpStatusValidationBuilder extends LeafNodeBuilder {

    @JsonProperty List<Integer> success = new ArrayList<>();
    @JsonProperty List<Integer> failed = new ArrayList<>();

    public HttpStatusValidationBuilder() {
        super(BuilderType.HttpStatusValidation);
    }

    public HttpStatusValidationBuilder success(Integer... statusCode) {
        success.addAll(List.of(statusCode));
        return this;
    }

    public HttpStatusValidationBuilder success(Integer fromStatusCodeInclusive, Integer toStatusCodeInclusive) {
        success.addAll(HttpStatusCode.range(fromStatusCodeInclusive, toStatusCodeInclusive).stream().map(HttpStatusCode::statusCode).collect(Collectors.toList()));
        return this;
    }

    public HttpStatusValidationBuilder fail(Integer... statusCode) {
        failed.addAll(List.of(statusCode));
        return this;
    }

    public HttpStatusValidationBuilder fail(Integer fromStatusCodeInclusive, Integer toStatusCodeInclusive) {
        failed.addAll(HttpStatusCode.range(fromStatusCodeInclusive, toStatusCodeInclusive).stream().map(HttpStatusCode::statusCode).collect(Collectors.toList()));
        return this;
    }

    @Override
    <R extends Base> R build(BuildContext buildContext) {
        return (R) new HttpStatusValidationNode(success, failed);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        HttpStatusValidationBuilder that = (HttpStatusValidationBuilder) o;
        return Objects.equals(success, that.success) &&
                Objects.equals(failed, that.failed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), success, failed);
    }

    static class HttpStatusValidationNode extends LeafNode implements HttpStatusValidation {

        final List<HttpStatusCode> success;
        final List<HttpStatusCode> failed;

        HttpStatusValidationNode(List<Integer> success, List<Integer> failed) {
            this.success = success.stream().map(HttpStatusCode::valueOf).collect(Collectors.toList());
            this.failed = failed.stream().map(HttpStatusCode::valueOf).collect(Collectors.toList());
        }

        @Override
        public List<HttpStatusCode> success() {
            return success;
        }

        @Override
        public List<HttpStatusCode> failed() {
            return failed;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            HttpStatusValidationNode that = (HttpStatusValidationNode) o;
            return Objects.equals(success, that.success) &&
                    Objects.equals(failed, that.failed);
        }

        @Override
        public int hashCode() {
            return Objects.hash(success, failed);
        }

        @Override
        public String toString() {
            return "HttpStatusNode{" +
                    "success=" + success +
                    ", failed=" + failed +
                    '}';
        }
    }
}
