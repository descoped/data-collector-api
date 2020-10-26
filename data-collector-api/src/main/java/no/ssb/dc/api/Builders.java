package no.ssb.dc.api;

import no.ssb.dc.api.node.builder.AddContentBuilder;
import no.ssb.dc.api.node.builder.BodyBuilder;
import no.ssb.dc.api.node.builder.BodyContainsBuilder;
import no.ssb.dc.api.node.builder.BodyPublisherBuilder;
import no.ssb.dc.api.node.builder.ConsoleBuilder;
import no.ssb.dc.api.node.builder.DeleteBuilder;
import no.ssb.dc.api.node.builder.EvalBuilder;
import no.ssb.dc.api.node.builder.ExecuteBuilder;
import no.ssb.dc.api.node.builder.ForEachBuilder;
import no.ssb.dc.api.node.builder.GetBuilder;
import no.ssb.dc.api.node.builder.HttpStatusValidationBuilder;
import no.ssb.dc.api.node.builder.JqPathBuilder;
import no.ssb.dc.api.node.builder.JsonTokenBuilder;
import no.ssb.dc.api.node.builder.JwtClaims;
import no.ssb.dc.api.node.builder.JwtHeaderClaims;
import no.ssb.dc.api.node.builder.JwtIdentityBuilder;
import no.ssb.dc.api.node.builder.JwtIdentityTokenBodyPublisherProducerBuilder;
import no.ssb.dc.api.node.builder.NextPageBuilder;
import no.ssb.dc.api.node.builder.PaginateBuilder;
import no.ssb.dc.api.node.builder.ParallelBuilder;
import no.ssb.dc.api.node.builder.PostBuilder;
import no.ssb.dc.api.node.builder.ProcessBuilder;
import no.ssb.dc.api.node.builder.PublishBuilder;
import no.ssb.dc.api.node.builder.PutBuilder;
import no.ssb.dc.api.node.builder.QueryBuilder;
import no.ssb.dc.api.node.builder.RegExBuilder;
import no.ssb.dc.api.node.builder.SecurityBuilder;
import no.ssb.dc.api.node.builder.SequenceBuilder;
import no.ssb.dc.api.node.builder.SpecificationContextBuilder;
import no.ssb.dc.api.node.builder.WhenExpressionIsTrueBuilder;
import no.ssb.dc.api.node.builder.WhenVariableIsNullBuilder;
import no.ssb.dc.api.node.builder.XPathBuilder;

public class Builders {

    public static SpecificationContextBuilder context() {
        return new SpecificationContextBuilder();
    }

    public static SecurityBuilder security() {
        return new SecurityBuilder();
    }

    public static JwtIdentityBuilder jwt(String id, JwtHeaderClaims headerClaims, JwtClaims claims) {
        return new JwtIdentityBuilder(id, headerClaims, claims);
    }

    public static JwtIdentityTokenBodyPublisherProducerBuilder jwtToken() {
        return new JwtIdentityTokenBodyPublisherProducerBuilder();
    }

    public static JwtHeaderClaims headerClaims() {
        return new JwtHeaderClaims();
    }

    public static JwtClaims claims() {
        return new JwtClaims();
    }

    public static GetBuilder get(String functionId) {
        return new GetBuilder(functionId);
    }

    public static PostBuilder post(String functionId) {
        return new PostBuilder(functionId);
    }

    public static PutBuilder put(String functionId) {
        return new PutBuilder(functionId);
    }

    public static DeleteBuilder delete(String functionId) {
        return new DeleteBuilder(functionId);
    }

    public static HttpStatusValidationBuilder status() {
        return new HttpStatusValidationBuilder();
    }

    public static BodyPublisherBuilder bodyPublisher() {
        return new BodyPublisherBuilder();
    }

    public static BodyContainsBuilder bodyContains(QueryBuilder queryBuilder, String equalToStringLiteral) {
        return new BodyContainsBuilder(queryBuilder, equalToStringLiteral);
    }

    public static PaginateBuilder paginate(String function) {
        return new PaginateBuilder(function);
    }

    /**
     * A sequence determines expected-positions from remote api
     *
     * @param builder
     * @return
     */
    public static SequenceBuilder sequence(QueryBuilder builder) {
        return new SequenceBuilder(builder);
    }

    public static NextPageBuilder nextPage() {
        return new NextPageBuilder();
    }

    public static ParallelBuilder parallel(QueryBuilder builder) {
        return new ParallelBuilder(builder);
    }

    public static ForEachBuilder forEach(QueryBuilder builder) {
        return new ForEachBuilder(builder);
    }

    public static ExecuteBuilder execute(String function) {
        return new ExecuteBuilder(function);
    }

    public static ProcessBuilder process(Class<? extends Processor> processorClass) {
        return new ProcessBuilder(processorClass);
    }

    public static AddContentBuilder addContent(String positionVariable, String contentKey) {
        return new AddContentBuilder(positionVariable, contentKey);
    }

    public static PublishBuilder publish(String positionVariable) {
        return new PublishBuilder(positionVariable);
    }

    public static BodyBuilder body() {
        return new BodyBuilder();
    }

    public static EvalBuilder eval(QueryBuilder queryBuilder, String bindTo, String elExpression) {
        return new EvalBuilder(queryBuilder, bindTo, elExpression);
    }

    public static XPathBuilder xpath(String expression) {
        return new XPathBuilder(expression);
    }

    public static JsonTokenBuilder jsonToken() {
        return new JsonTokenBuilder();
    }

    public static JqPathBuilder jqpath(String expression) {
        return new JqPathBuilder(expression);
    }

    public static RegExBuilder regex(QueryBuilder queryBuilder, String expression) {
        return new RegExBuilder(queryBuilder, expression);
    }

    public static WhenVariableIsNullBuilder whenVariableIsNull(String identifier) {
        return new WhenVariableIsNullBuilder().identifier(identifier);
    }

    public static WhenExpressionIsTrueBuilder whenExpressionIsTrue(String expression) {
        return new WhenExpressionIsTrueBuilder().identifier(expression);
    }

    public static ConsoleBuilder console() {
        return new ConsoleBuilder();
    }
}
