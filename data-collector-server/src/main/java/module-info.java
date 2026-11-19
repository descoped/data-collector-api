module io.descoped.dc.server {

    requires jdk.unsupported;

    requires io.descoped.service.provider.api;
    requires transitive io.descoped.dynamic.config;
    requires transitive io.descoped.dc.api;
    requires transitive io.descoped.dc.core;
    requires transitive io.descoped.dc.application;
    requires io.descoped.dc.content.rawdata;
    requires io.descoped.rawdata.api;
    requires io.descoped.secrets.api;
    // The rawdata and secrets providers are runtime dependencies: their API modules declare `uses`, so the
    // providers resolve on the module path without being required.

    requires java.instrument;

    requires net.bytebuddy;
    requires net.bytebuddy.agent;
    requires org.slf4j;
    requires jul.to.slf4j;
    requires com.fasterxml.jackson.databind;
    requires io.github.classgraph;
    requires org.apache.tika.core;

    requires java.net.http;
    requires transitive undertow.core;

    requires transitive lmdbjava;

    opens io.descoped.dc.server;
    opens worker.config;

    opens io.descoped.dc.server.content to com.fasterxml.jackson.databind, org.apache.tika.core;
    opens io.descoped.dc.server.task to com.fasterxml.jackson.databind;
    opens io.descoped.dc.server.integrity to com.fasterxml.jackson.databind;
    opens io.descoped.dc.server.recovery to com.fasterxml.jackson.databind;

    exports io.descoped.dc.server;
    exports io.descoped.dc.server.content;
    exports io.descoped.dc.server.db;
    exports io.descoped.dc.server.integrity;
    exports io.descoped.dc.server.recovery;
    exports io.descoped.dc.server.ssl;
    exports io.descoped.dc.server.task;
}
