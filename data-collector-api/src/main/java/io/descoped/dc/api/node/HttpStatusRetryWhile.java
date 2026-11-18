package io.descoped.dc.api.node;

import java.util.concurrent.TimeUnit;

public interface HttpStatusRetryWhile extends Validator {

    Integer statusCode();

    TimeUnit duration();

    Integer amount();

    BodyContains bodyContains();

}
