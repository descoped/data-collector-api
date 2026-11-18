package io.descoped.dc.api.node.builder;

public enum BuilderType {
    Specification(SpecificationBuilder.class),
    SpecificationContext(SpecificationContextBuilder.class),
    Security(SecurityBuilder.class),
    JwtIdentity(JwtIdentityBuilder.class),
    JwtIdentityTokenBodyPublisherProducer(JwtIdentityTokenBodyPublisherProducerBuilder.class),
    Paginate(PaginateBuilder.class),
    ForEach(ForEachBuilder.class),
    Sequence(SequenceBuilder.class),
    NextPage(NextPageBuilder.class),
    Parallel(ParallelBuilder.class),
    Execute(ExecuteBuilder.class),
    Process(ProcessBuilder.class),
    QueryBody(BodyBuilder.class),
    QueryEval(EvalBuilder.class),
    QueryXPath(XPathBuilder.class),
    QueryJsonToken(JsonTokenBuilder.class),
    QueryXmlToken(XmlTokenBuilder.class),
    QueryJqPath(JqPathBuilder.class),
    QueryRegEx(RegExBuilder.class),
    ConditionWhenVariableIsNull(WhenVariableIsNullBuilder.class),
    ConditionWhenExpressionIsTrue(WhenExpressionIsTrueBuilder.class),
    AddContent(AddContentBuilder.class),
    Publish(PublishBuilder.class),
    Get(GetBuilder.class),
    Post(PostBuilder.class),
    Put(PutBuilder.class),
    Delete(DeleteBuilder.class),
    HttpStatusRetryWhile(HttpStatusRetryWhileBuilder.class),
    HttpStatusValidation(HttpStatusValidationBuilder.class),
    BodyPublisher(BodyPublisherBuilder.class),
    StringBodyPublisherProducer(StringBodyPublisherProducerBuilder.class),
    HttpResponseBodyContains(BodyContainsBuilder.class),
    Console(ConsoleBuilder.class);

    public final Class<? extends AbstractBuilder> builderClass;

    BuilderType(Class<? extends AbstractBuilder> builderClass) {
        this.builderClass = builderClass;
    }

    public static BuilderType parse(String name) {
        for (BuilderType builderType : values()) {
            if (builderType.name().equalsIgnoreCase(name)) {
                return builderType;
            }
        }
        return null;
    }
}
