package io.descoped.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.descoped.dc.api.http.HttpStatus;
import io.descoped.dc.api.node.Base;
import io.descoped.dc.api.node.HttpStatusValidation;
import io.descoped.dc.api.node.ResponsePredicate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class HttpStatusValidationBuilder extends LeafNodeBuilder {

    @JsonProperty
    Map<Integer, List<ResponsePredicateBuilder>> success = new LinkedHashMap<>();
    @JsonProperty
    List<Integer> failed = new ArrayList<>();

    public HttpStatusValidationBuilder() {
        super(BuilderType.HttpStatusValidation);
    }

    public HttpStatusValidationBuilder success(Integer... statusCode) {
        for (int sc : statusCode) {
            success.put(sc, new ArrayList<>());
        }
        return this;
    }

    public HttpStatusValidationBuilder success(Integer fromStatusCodeInclusive, Integer toStatusCodeInclusive) {
        List<Integer> statusCodes = HttpStatus.range(fromStatusCodeInclusive, toStatusCodeInclusive).stream().map(HttpStatus::code).collect(Collectors.toList());
        for (int sc : statusCodes) {
            success.put(sc, new ArrayList<>());
        }
        return this;
    }

    public HttpStatusValidationBuilder success(Integer statusCode, ResponsePredicateBuilder responsePredicateBuilder) {
        success.computeIfAbsent(statusCode, list -> new ArrayList<>()).add(responsePredicateBuilder);
        return this;
    }

    public HttpStatusValidationBuilder fail(Integer... statusCode) {
        failed.addAll(List.of(statusCode));
        return this;
    }

    public HttpStatusValidationBuilder fail(Integer fromStatusCodeInclusive, Integer toStatusCodeInclusive) {
        failed.addAll(HttpStatus.range(fromStatusCodeInclusive, toStatusCodeInclusive).stream().map(HttpStatus::code).collect(Collectors.toList()));
        return this;
    }

    @Override
    public <R extends Base> R build(BuildContext buildContext) {
        Map<HttpStatus, List<ResponsePredicate>> successMap = new LinkedHashMap<>();
        for (Map.Entry<Integer, List<ResponsePredicateBuilder>> entry : success.entrySet()) {
            List<ResponsePredicate> responsePredicateList = entry.getValue().stream()
                    .map(builder -> (ResponsePredicate) builder.build(buildContext)).collect(Collectors.toList());
            successMap.computeIfAbsent(HttpStatus.valueOf(entry.getKey()), list -> new ArrayList<>()).addAll(responsePredicateList);
        }
        return (R) new HttpStatusValidationNode(successMap, failed);
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

        final Map<HttpStatus, List<ResponsePredicate>> success;
        final List<HttpStatus> failed;

        HttpStatusValidationNode(Map<HttpStatus, List<ResponsePredicate>> success, List<Integer> failed) {
            this.success = success;
            this.failed = failed.stream().map(HttpStatus::valueOf).collect(Collectors.toList());
        }

        @Override
        public Map<HttpStatus, List<ResponsePredicate>> success() {
            return success;
        }

        @Override
        public List<HttpStatus> failed() {
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
            return "HttpStatusValidationNode{" +
                    "success=" + success +
                    ", failed=" + failed +
                    '}';
        }
    }
}
