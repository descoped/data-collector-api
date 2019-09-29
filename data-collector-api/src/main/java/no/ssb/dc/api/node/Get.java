package no.ssb.dc.api.node;

import java.util.List;

public interface Get extends Operation {

    String url();

    List<? extends Node> steps();

    List<String> returnVariables();

}
