package io.descoped.dc.api.health;

import java.util.Deque;
import java.util.Map;
import java.util.Optional;

public interface HealthResource {

    Optional<Boolean> isUp();

    String name();

    boolean isList();

    boolean canRender(Map<String, Deque<String>> queryParams);

    Object resource();

}
