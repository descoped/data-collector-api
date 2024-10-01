module io.descoped.dc.api {
    requires io.descoped.dynamic.config;
    requires io.descoped.service.provider.api;
    requires io.descoped.rawdata.api;

    requires org.slf4j;
    requires io.github.classgraph;
    requires de.huxhorn.sulky.ulid;
    requires commons.jexl3;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires java.xml;
    requires secrets.client.api;

    //opens io.descoped.dc.api.node.builder to com.fasterxml.jackson.databind;
    opens io.descoped.dc.api.node.builder;
    opens io.descoped.dc.api.http to com.fasterxml.jackson.databind;

    uses Client.Builder;
    uses Request.Builder;
    uses Response.Builder;

    exports io.descoped.dc.api;
    exports io.descoped.dc.api.context;
    exports io.descoped.dc.api.metrics;
    exports io.descoped.dc.api.health;
    exports io.descoped.dc.api.node;
    exports io.descoped.dc.api.node.builder;
    exports io.descoped.dc.api.el;
    exports io.descoped.dc.api.content;
    exports io.descoped.dc.api.handler;
    exports io.descoped.dc.api.http;
    exports io.descoped.dc.api.ulid;
    exports io.descoped.dc.api.error;
    exports io.descoped.dc.api.security;
    exports io.descoped.dc.api.services;
    exports io.descoped.dc.api.util;
}
