package no.ssb.dc.api;

import no.ssb.dc.api.builder.AddContentBuilder;
import no.ssb.dc.api.builder.EvalBuilder;
import no.ssb.dc.api.builder.ExecuteBuilder;
import no.ssb.dc.api.builder.GetBuilder;
import no.ssb.dc.api.builder.NextPageBuilder;
import no.ssb.dc.api.builder.PaginateBuilder;
import no.ssb.dc.api.builder.ParallelBuilder;
import no.ssb.dc.api.builder.ProcessBuilder;
import no.ssb.dc.api.builder.PublishBuilder;
import no.ssb.dc.api.builder.QueryBuilder;
import no.ssb.dc.api.builder.RegExBuilder;
import no.ssb.dc.api.builder.SequenceBuilder;
import no.ssb.dc.api.builder.WhenVariableIsNullBuilder;
import no.ssb.dc.api.builder.XPathBuilder;

public class Builders {

    public static GetBuilder get(String nodeId) {
        return new GetBuilder(nodeId);
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

    public static ExecuteBuilder execute(String id) {
        return new ExecuteBuilder(id);
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
