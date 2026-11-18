module io.descoped.dc.content.discarding {

    requires io.descoped.service.provider.api;
    requires io.descoped.dc.api;

    requires org.slf4j;
    requires de.huxhorn.sulky.ulid;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;

    provides io.descoped.dc.api.content.ContentStoreInitializer with io.descoped.dc.content.provider.discarding.DiscardingContentStoreInitializer;

    exports io.descoped.dc.content.provider.discarding;
}
