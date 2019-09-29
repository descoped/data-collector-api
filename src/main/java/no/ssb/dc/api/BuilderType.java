package no.ssb.dc.api;

public enum BuilderType {
    Flow,
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
    Get;

    public static BuilderType parse(String name) {
        for (BuilderType builderType : values()) {
            if (builderType.name().equalsIgnoreCase(name)) {
                return builderType;
            }
        }
        return null;
    }
}