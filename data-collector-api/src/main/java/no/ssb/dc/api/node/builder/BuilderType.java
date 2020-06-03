package no.ssb.dc.api.node.builder;

public enum BuilderType {
    Specification,
    SpecificationContext,
    Security,
    Paginate,
    Sequence,
    NextPage,
    Parallel,
    Execute,
    Process,
    QueryEval,
    QueryXPath,
    QueryJqPath,
    QueryRegEx,
    ConditionWhenVariableIsNull,
    ConditionWhenExpressionIsTrue,
    AddContent,
    Publish,
    Get,
    Post,
    Put,
    Delete,
    HttpStatusValidation,
    BodyPublisher,
    HttpResponseBodyContains;

    public static BuilderType parse(String name) {
        for (BuilderType builderType : values()) {
            if (builderType.name().equalsIgnoreCase(name)) {
                return builderType;
            }
        }
        return null;
    }
}
