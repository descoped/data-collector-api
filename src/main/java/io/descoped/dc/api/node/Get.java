package io.descoped.dc.api.node;

import java.util.List;

public interface Get extends Operation {

    List<HttpStatusRetryWhile> retryWhile();

}
