package no.ssb.dc.api;

import no.ssb.dc.api.node.builder.AddContentBuilder;
import no.ssb.dc.api.node.builder.EvalBuilder;
import no.ssb.dc.api.node.builder.ExecuteBuilder;
import no.ssb.dc.api.node.builder.FlowContextBuilder;
import no.ssb.dc.api.node.builder.GetBuilder;
import no.ssb.dc.api.node.builder.HttpStatusValidationBuilder;
import no.ssb.dc.api.node.builder.NextPageBuilder;
import no.ssb.dc.api.node.builder.PaginateBuilder;
import no.ssb.dc.api.node.builder.ParallelBuilder;
import no.ssb.dc.api.node.builder.ProcessBuilder;
import no.ssb.dc.api.node.builder.PublishBuilder;
import no.ssb.dc.api.node.builder.QueryBuilder;
import no.ssb.dc.api.node.builder.RegExBuilder;
import no.ssb.dc.api.node.builder.SecurityBuilder;
import no.ssb.dc.api.node.builder.SequenceBuilder;
import no.ssb.dc.api.node.builder.WhenVariableIsNullBuilder;
import no.ssb.dc.api.node.builder.XPathBuilder;

public class Builders {

    public static FlowContextBuilder context() {
        return new FlowContextBuilder();
    }

    public static SecurityBuilder security() {
        return new SecurityBuilder();
    }

    public static GetBuilder get(String nodeId) {
        return new GetBuilder(nodeId);
    }

    public static HttpStatusValidationBuilder status() {
        return new HttpStatusValidationBuilder();
    }

    public static PaginateBuilder paginate(String nodeId) {
        return new PaginateBuilder(nodeId);
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

    public static ExecuteBuilder execute(String executeId) {
        return new ExecuteBuilder(executeId);
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

    public static EvalBuilder eval(QueryBuilder queryBuilder, String bindTo, String elExpression) {
        return new EvalBuilder(queryBuilder, bindTo, elExpression);
    }

    public static XPathBuilder xpath(String expression) {
        return new XPathBuilder(expression);
    }

    public static RegExBuilder regex(QueryBuilder queryBuilder, String expression) {
        return new RegExBuilder(queryBuilder, expression);
    }

    public static WhenVariableIsNullBuilder whenVariableIsNull(String identifier) {
        return new WhenVariableIsNullBuilder().identifier(identifier);
    }
}
