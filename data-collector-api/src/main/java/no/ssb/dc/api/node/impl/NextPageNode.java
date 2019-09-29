package no.ssb.dc.api.node.impl;

import no.ssb.dc.api.node.NextPage;
import no.ssb.dc.api.node.Node;
import no.ssb.dc.api.node.Query;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class NextPageNode extends FlowNode implements NextPage {

    private final Map<String, QueryNode> queryNodeMap;

    public NextPageNode(Map<String, QueryNode> queryNodeMap) {
        this.queryNodeMap = queryNodeMap;
    }

    @Override
    public Map<String, Query> outputs() {
        return queryNodeMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> (Query) y, LinkedHashMap::new));
    }

    @Override
    public Iterator<? extends Node> iterator() {
        return createNodeList().iterator();
    }
}
