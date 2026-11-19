module io.descoped.dc.content.discarding {

    requires io.descoped.service.provider.api;
    requires transitive io.descoped.dc.api;

    requires org.slf4j;

    provides io.descoped.dc.api.content.ContentStoreInitializer with io.descoped.dc.content.provider.discarding.DiscardingContentStoreInitializer;

    exports io.descoped.dc.content.provider.discarding;
}
