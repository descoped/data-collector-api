package no.ssb.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import no.ssb.dc.api.http.HttpStatusCode;
import no.ssb.dc.api.node.Base;
import no.ssb.dc.api.node.ValidateResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class ValidateResponseBuilder<T extends OperationBuilder> extends LeafNodeBuilder {

    private final T parentBuilder;
    @JsonProperty List<Integer> success = new ArrayList<>();
    @JsonProperty List<Integer> failed = new ArrayList<>();

    public ValidateResponseBuilder(T parentBuilder) {
        super(BuilderType.ValidateResponse);
        this.parentBuilder = parentBuilder;
    }

    public T success(Integer... statusCode) {
        success.addAll(List.of(statusCode));
        return parentBuilder;
    }

    public T success(Integer fromStatusCodeInclusive, Integer toStatusCodeInclusive) {
        success.addAll(HttpStatusCode.range(fromStatusCodeInclusive, toStatusCodeInclusive).stream().map(HttpStatusCode::statusCode).collect(Collectors.toList()));
        return parentBuilder;
    }

    public T fail(Integer... statusCode) {
        failed.addAll(List.of(statusCode));
        return parentBuilder;
    }

    public T fail(Integer fromStatusCodeInclusive, Integer toStatusCodeInclusive) {
        failed.addAll(HttpStatusCode.range(fromStatusCodeInclusive, toStatusCodeInclusive).stream().map(HttpStatusCode::statusCode).collect(Collectors.toList()));
        return parentBuilder;
    }

    @Override
    <R extends Base> R build(BuildContext buildContext) {
        return (R) new ValidateResponseNode(success, failed);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ValidateResponseBuilder that = (ValidateResponseBuilder) o;
        return Objects.equals(success, that.success) &&
                Objects.equals(failed, that.failed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), success, failed);
    }

    @Override
    public String toString() {
        return "ValidateResponseBuilder{" +
                "success=" + success +
                ", failed=" + failed +
                '}';
    }

    static class ValidateResponseNode extends LeafNode implements ValidateResponse {

        final List<HttpStatusCode> success;
        final List<HttpStatusCode> failed;

        ValidateResponseNode(List<Integer> success, List<Integer> failed) {
            this.success = success.stream().map(code -> HttpStatusCode.valueOf(code)).collect(Collectors.toList());
            this.failed = failed.stream().map(code -> HttpStatusCode.valueOf(code)).collect(Collectors.toList());
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
            ValidateResponseNode that = (ValidateResponseNode) o;
            return Objects.equals(success, that.success) &&
                    Objects.equals(failed, that.failed);
        }

        @Override
        public int hashCode() {
            return Objects.hash(success, failed);
        }

        @Override
        public String toString() {
            return "ValidateResponseNode{" +
                    "success=" + success +
                    ", failed=" + failed +
                    '}';
        }
    }
}
