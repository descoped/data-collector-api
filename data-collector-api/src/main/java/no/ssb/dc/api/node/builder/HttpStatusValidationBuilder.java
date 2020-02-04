package no.ssb.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import no.ssb.dc.api.http.HttpStatusCode;
import no.ssb.dc.api.node.Base;
import no.ssb.dc.api.node.HttpStatusValidation;
import no.ssb.dc.api.node.ResponsePredicate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class HttpStatusValidationBuilder extends LeafNodeBuilder {

    @JsonProperty Map<Integer, List<ResponsePredicateBuilder>> success = new LinkedHashMap<>();
    @JsonProperty List<Integer> failed = new ArrayList<>();

    public HttpStatusValidationBuilder() {
        super(BuilderType.HttpStatusValidation);
    }

    public HttpStatusValidationBuilder success(Integer... statusCode) {
        for (int sc : statusCode) {
            success.put(sc, Collections.emptyList());
        }
        return this;
    }

    public HttpStatusValidationBuilder success(Integer fromStatusCodeInclusive, Integer toStatusCodeInclusive) {
        List<Integer> statusCodes = HttpStatusCode.range(fromStatusCodeInclusive, toStatusCodeInclusive).stream().map(HttpStatusCode::statusCode).collect(Collectors.toList());
        for (int sc : statusCodes) {
            success.put(sc, Collections.emptyList());
        }
        return this;
    }

    public HttpStatusValidationBuilder success(Integer statusCode, BodyContainsBuilder bodyContains) {
        success.computeIfAbsent(statusCode, list -> new ArrayList<>()).add(bodyContains);
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
    public <R extends Base> R build(BuildContext buildContext) {
        Map<HttpStatusCode, List<ResponsePredicate>> successMap = new LinkedHashMap<>();
        for (Map.Entry<Integer, List<ResponsePredicateBuilder>> entry : success.entrySet()) {
            List<ResponsePredicate> responsePredicateList = entry.getValue().stream()
                    .map(builder -> (ResponsePredicate) builder.build(buildContext)).collect(Collectors.toList());
            successMap.computeIfAbsent(HttpStatusCode.valueOf(entry.getKey()), list -> new ArrayList<>()).addAll(responsePredicateList);
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

        final Map<HttpStatusCode, List<ResponsePredicate>> success;
        final List<HttpStatusCode> failed;

        HttpStatusValidationNode(Map<HttpStatusCode, List<ResponsePredicate>> success, List<Integer> failed) {
            this.success = success;
            this.failed = failed.stream().map(HttpStatusCode::valueOf).collect(Collectors.toList());
        }

        @Override
        public Map<HttpStatusCode, List<ResponsePredicate>> success() {
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
            return "HttpStatusValidationNode{" +
                    "success=" + success +
                    ", failed=" + failed +
                    '}';
        }
    }
}
