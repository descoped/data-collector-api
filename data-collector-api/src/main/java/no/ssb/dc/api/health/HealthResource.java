package no.ssb.dc.api.health;

import java.util.Deque;
import java.util.Map;

public interface HealthResource {

    String name();

    boolean isList();

    boolean canRender(Map<String, Deque<String>> queryParams);

    Object resource();

}
