package io.descoped.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.descoped.dc.api.http.HttpStatus;
import io.descoped.dc.api.node.Base;
import io.descoped.dc.api.node.BodyContains;
import io.descoped.dc.api.node.HttpStatusRetryWhile;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class HttpStatusRetryWhileBuilder extends LeafNodeBuilder {

    @JsonProperty("statusCode")
    public Integer statusCode;
    @JsonProperty("duration")
    public TimeUnit duration;
    @JsonProperty("amount")
    public Integer amount;
    @JsonProperty("bodyContains")
    BodyContainsBuilder bodyContainsBuilder;

    public HttpStatusRetryWhileBuilder() {
        super(BuilderType.HttpStatusRetryWhile);
    }

    public HttpStatusRetryWhileBuilder is(Integer statusCode, TimeUnit duration, Integer amount) {
        this.statusCode = statusCode;
        this.duration = duration;
        this.amount = amount;
        return this;
    }

    public HttpStatusRetryWhileBuilder is(HttpStatus statusCode, TimeUnit duration, Integer amount) {
        this.statusCode = statusCode.code();
        this.duration = duration;
        this.amount = amount;
        return this;
    }

    /*
    TODO: implement regex predicate support
    public HttpStatusRetryWhileBuilder bodyContains(QueryBuilder equalToQuery) {
        this.bodyContainsBuilder = Builders.bodyContains(equalToQuery);
        return this;
    }
    */

    @Override
    public <R extends Base> R build(BuildContext buildContext) {
        BodyContains bodyContains = bodyContainsBuilder != null ? bodyContainsBuilder.build(buildContext) : null;
        return (R) new HttpStatusRetryWhileNode(statusCode, duration, amount, bodyContains);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        HttpStatusRetryWhileBuilder that = (HttpStatusRetryWhileBuilder) o;
        return statusCode.equals(that.statusCode) &&
                duration == that.duration &&
                Objects.equals(amount, that.amount) &&
                Objects.equals(bodyContainsBuilder, that.bodyContainsBuilder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), statusCode, duration, amount, bodyContainsBuilder);
    }

    @Override
    public String toString() {
        return "HttpStatusRetryWhileBuilder{" +
                "statusCode=" + statusCode +
                ", duration=" + duration +
                ", amount=" + amount +
                ", bodyContainsBuilder=" + bodyContainsBuilder +
                '}';
    }

    static class HttpStatusRetryWhileNode extends LeafNode implements HttpStatusRetryWhile {

        final Integer statusCode;
        final TimeUnit duration;
        final Integer amount;
        final BodyContains bodyContains;

        HttpStatusRetryWhileNode(Integer statusCode, TimeUnit duration, Integer amount, BodyContains bodyContains) {
            this.statusCode = statusCode;
            this.duration = duration;
            this.amount = amount;
            this.bodyContains = bodyContains;
        }

        @Override
        public Integer statusCode() {
            return statusCode;
        }

        @Override
        public TimeUnit duration() {
            return duration;
        }

        @Override
        public Integer amount() {
            return amount;
        }

        @Override
        public BodyContains bodyContains() {
            return bodyContains;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            HttpStatusRetryWhileNode that = (HttpStatusRetryWhileNode) o;
            return statusCode.equals(that.statusCode) &&
                    duration == that.duration &&
                    Objects.equals(amount, that.amount);
        }

        @Override
        public int hashCode() {
            return Objects.hash(statusCode, duration, amount);
        }

        @Override
        public String toString() {
            return "HttpStatusRetryWhileNode{" +
                    "statusCode=" + statusCode +
                    ", duration=" + duration +
                    ", amount=" + amount +
                    '}';
        }
    }

}
