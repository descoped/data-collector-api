import no.ssb.dc.api.http.Client;
import no.ssb.dc.api.http.Request;
import no.ssb.dc.api.http.Response;

module no.ssb.dc.api {
    requires no.ssb.config;
    requires no.ssb.service.provider.api;
    requires no.ssb.rawdata.api;

    requires org.slf4j;
    requires io.github.classgraph;
    requires de.huxhorn.sulky.ulid;
    requires commons.jexl3;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires java.xml;

    //opens no.ssb.dc.api.node.builder to com.fasterxml.jackson.databind;
    opens no.ssb.dc.api.node.builder;
    opens no.ssb.dc.api.http to com.fasterxml.jackson.databind;

    uses Client.Builder;
    uses Request.Builder;
    uses Response.Builder;

    exports no.ssb.dc.api;
    exports no.ssb.dc.api.context;
    exports no.ssb.dc.api.metrics;
    exports no.ssb.dc.api.health;
    exports no.ssb.dc.api.node;
    exports no.ssb.dc.api.node.builder;
    exports no.ssb.dc.api.el;
    exports no.ssb.dc.api.content;
    exports no.ssb.dc.api.handler;
    exports no.ssb.dc.api.http;
    exports no.ssb.dc.api.ulid;
    exports no.ssb.dc.api.error;
    exports no.ssb.dc.api.services;
    exports no.ssb.dc.api.util;
}
