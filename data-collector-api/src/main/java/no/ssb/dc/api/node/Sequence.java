package no.ssb.dc.api.node;

public interface Sequence extends Node {

    Query splitToListQuery();

    Query expectedQuery();

}
