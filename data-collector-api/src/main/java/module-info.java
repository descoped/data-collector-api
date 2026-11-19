module io.descoped.dc.api {
    // Types of these modules appear in this module's exported API.
    requires transitive io.descoped.service.provider.api;
    requires transitive de.huxhorn.sulky.ulid;
    requires transitive com.fasterxml.jackson.databind;

    requires org.slf4j;
    requires commons.jexl3;
    requires com.fasterxml.jackson.annotation;
    requires transitive com.fasterxml.jackson.dataformat.yaml;

    //opens io.descoped.dc.api.node.builder to com.fasterxml.jackson.databind;
    opens io.descoped.dc.api.node.builder;
    opens io.descoped.dc.api.http to com.fasterxml.jackson.databind;

    uses io.descoped.dc.api.http.Client.Builder;
    uses io.descoped.dc.api.http.Request.Builder;
    uses io.descoped.dc.api.http.Response.Builder;

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
