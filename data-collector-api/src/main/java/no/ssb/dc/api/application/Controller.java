package no.ssb.dc.api.application;

import io.undertow.server.HttpHandler;

public interface Controller extends HttpHandler {

    String contextPath();

}
