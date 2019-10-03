package no.ssb.dc.api.node.builder;

public enum BuilderType {
    Flow,
    FlowContext,
    Paginate,
    Sequence,
    NextPage,
    Parallel,
    Execute,
    Process,
    QueryEval,
    QueryXPath,
    QueryRegEx,
    ConditionWhenVariableIsNull,
    AddContent,
    Publish,
    Get,
    HttpStatusValidation;

    public static BuilderType parse(String name) {
        for (BuilderType builderType : values()) {
            if (builderType.name().equalsIgnoreCase(name)) {
                return builderType;
            }
        }
        return null;
    }
}
