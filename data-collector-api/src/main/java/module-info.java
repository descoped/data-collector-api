import no.ssb.dc.api.content.ContentStoreInitializer;
import no.ssb.dc.api.http.Client;
import no.ssb.dc.api.http.Request;
import no.ssb.dc.api.http.Response;
import no.ssb.dc.content.DiscardingContentStoreInitializer;

module no.ssb.dc.api {
    requires no.ssb.config;
    requires no.ssb.service.provider.api;
    requires no.ssb.rawdata.api;

    requires org.slf4j;
    requires io.github.classgraph;
    requires de.huxhorn.sulky.ulid;
    requires commons.jexl3;

    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.dataformat.yaml;

    requires java.xml;

    requires org.bouncycastle.pkix;
    requires org.bouncycastle.provider;

    uses Client.Builder;
    uses Request.Builder;
    uses Response.Builder;

    provides ContentStoreInitializer with DiscardingContentStoreInitializer;

    exports no.ssb.dc.api;
    exports no.ssb.dc.api.context;
    exports no.ssb.dc.api.node;
    exports no.ssb.dc.api.node.builder;
    exports no.ssb.dc.api.el;
    exports no.ssb.dc.api.content;
    exports no.ssb.dc.api.handler;
    exports no.ssb.dc.api.http;
    exports no.ssb.dc.api.ulid;
    exports no.ssb.dc.api.error;
    exports no.ssb.dc.api.security;
    exports no.ssb.dc.api.services;
    exports no.ssb.dc.api.util;
    exports no.ssb.dc.content;
}
