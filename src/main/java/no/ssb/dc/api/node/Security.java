package no.ssb.dc.api.node;

import java.util.List;

public interface Security extends Configuration {

    String sslBundleName();

    List<Identity> identities();

}
