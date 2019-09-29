package no.ssb.dc.api.http;

import java.util.ServiceLoader;

public interface Client {

    static Client newClient() {
        return newClientBuilder().build();
    }

    static Builder newClientBuilder() {
        return ServiceLoader.load(Client.Builder.class).findFirst().orElseThrow();
    }

    Response send(Request request);

    Object getDelegate();

    enum Version {
        HTTP_1_1,
        HTTP_2
    }

    interface Builder {
        Builder version(Version version);

        Client build();
    }

}
