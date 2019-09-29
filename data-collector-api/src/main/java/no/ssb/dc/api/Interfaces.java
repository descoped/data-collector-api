package no.ssb.dc.api;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface Interfaces {

    // used by all simple nodes
    interface BaseNode {
        boolean instanceOf(Class<? extends BaseNode> nodeClass);

        <R extends BaseNode> R cast(Class<R> nodeClass);

        <R extends BaseNode> void given(Class<R> isInstanceOfNodeClass, Consumer<R> then);

        <R extends BaseNode> void given(Class<R> isInstanceOfNodeClass, AtomicBoolean andIfNotHandled, Consumer<R> then);
    }

    // implements FlowNode and step-nodes
    interface Node extends BaseNode {
        Iterator<? extends Node> iterator();

        void traverse(int depth, Set<Node> visitedNodeIds, List<Node> ancestors, BiConsumer<List<Node>, Node> visit);

        String toPrintableExecutionPlan();
    }

    // identifiable nodes
    interface NodeWithId extends Node {
        String id();
    }

    // common interface for http operations
    interface Operation extends NodeWithId {
    }

    interface Get extends Operation {
        String url();

        List<? extends Node> steps();

        List<String> returnVariables();
    }

    interface Paginate extends Node {
        Set<String> variableNames();

        String variable(String name);

        boolean addPageContent();

        List<Execute> targets();

        double threshold();

        Condition condition();
    }

    interface Execute extends Node {
        String executeId();

        List<String> requiredInputs();

        Map<String, Query> inputVariable();

        Operation target();
    }

    interface Sequence extends Node {
        Query splitToListQuery();

        Query expectedQuery();
    }

    interface NextPage extends Node {
        Map<String, Query> outputs();
    }

    interface Parallel extends Node {
        Query splitQuery();

        Set<String> variableNames();

        Query variable(String name);

        List<Node> steps();

        Publish publish();
    }

    interface Process extends Node {
        Class<? extends Processor> processorClass();

        Set<String> requiredOutputs();

    }

    interface AddContent extends Node {
        String positionVariableExpression();
        String contentKey();
    }

    interface Publish extends Node {
        String positionVariableExpression();
    }

    interface Query extends BaseNode {
        String expression();

    }

    interface Condition extends BaseNode {
        String identifier();
    }

    interface Eval extends Query {
        Query query();
        String bind();
    }

    interface XPath extends Query {
    }

    interface RegEx extends Query {
        Query query();
    }

    interface WhenVariableIsNull extends Condition {

    }
}
