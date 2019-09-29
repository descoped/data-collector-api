package no.ssb.dc.api.node;

import java.util.Map;

public interface NextPage extends Node {

    Map<String, Query> outputs();

}
