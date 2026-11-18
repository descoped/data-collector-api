package io.descoped.dc.api.node;

import java.util.List;
import java.util.Set;

public interface Paginate extends NodeWithId {

    Set<String> variableNames();

    String variable(String name);

    boolean addPageContent();

    String positionVariable();

    List<Execute> targets();

    int threshold();

    Condition condition();

}
