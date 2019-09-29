package no.ssb.dc.api;

public class Builders {

    public static Flow.GetBuilder get(String nodeId) {
        return new Flow.GetBuilder(nodeId);
    }

    public static Flow.PaginateBuilder paginate(String nodeId) {
        return new Flow.PaginateBuilder(nodeId);
    }

    /**
     * A sequence determines expected-positions from remote api
     *
     * @param builder
     * @return
     */
    public static Flow.SequenceBuilder sequence(Flow.QueryBuilder builder) {
        return new Flow.SequenceBuilder(builder);
    }

    public static Flow.NextPageBuilder nextPage() {
        return new Flow.NextPageBuilder();
    }

    public static Flow.ParallelBuilder parallel(Flow.QueryBuilder builder) {
        return new Flow.ParallelBuilder(builder);
    }

    public static Flow.ExecuteBuilder execute(String id) {
        return new Flow.ExecuteBuilder(id);
    }

    public static Flow.ProcessBuilder process(Class<? extends Processor> processorClass) {
        return new Flow.ProcessBuilder(processorClass);
    }

    public static Flow.AddContentBuilder addContent(String positionVariable, String contentKey) {
        return new Flow.AddContentBuilder(positionVariable, contentKey);
    }

    public static Flow.PublishBuilder publish(String positionVariable) {
        return new Flow.PublishBuilder(positionVariable);
    }

    public static Flow.EvalBuilder eval(Flow.QueryBuilder queryBuilder, String bindTo, String elExpression) {
        return new Flow.EvalBuilder(queryBuilder, bindTo, elExpression);
    }

    public static Flow.XPathBuilder xpath(String expression) {
        return new Flow.XPathBuilder(expression);
    }

    public static Flow.RegExBuilder regex(Flow.QueryBuilder queryBuilder, String expression) {
        return new Flow.RegExBuilder(queryBuilder, expression);
    }

    public static Flow.WhenVariableIsNullBuilder whenVariableIsNull(String identifier) {
        return new Flow.WhenVariableIsNullBuilder().identifier(identifier);
    }
}
