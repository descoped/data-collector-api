package no.ssb.dc.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

class Nodes {

    private Nodes() {
    }

    abstract static class AbstractBaseNode implements Interfaces.BaseNode {
        @Override
        public boolean instanceOf(Class<? extends Interfaces.BaseNode> nodeClass) {
            return this.getClass().isAssignableFrom(nodeClass);
        }

        @Override
        public <R extends Interfaces.BaseNode> R cast(Class<R> nodeClass) {
            return nodeClass.cast(this);
        }

        @Override
        public <R extends Interfaces.BaseNode> void given(Class<R> isInstanceOfNodeClass, Consumer<R> then) {
            if (instanceOf(isInstanceOfNodeClass)) {
                then.accept(cast(isInstanceOfNodeClass));
            }
        }

        @Override
        public <R extends Interfaces.BaseNode> void given(Class<R> isInstanceOfNodeClass, AtomicBoolean andIfNotHandled, Consumer<R> then) {
            if (instanceOf(isInstanceOfNodeClass) && !andIfNotHandled.get()) {
                then.accept(cast(isInstanceOfNodeClass));
                andIfNotHandled.set(true);
            }
        }
    }

    abstract static class FlowNode extends Nodes.AbstractBaseNode implements Interfaces.Node {

        static void depthFirstPreOrderFullTraversal(int depth, Set<Interfaces.Node> visitedNodeIds,
                                                    List<Interfaces.Node> ancestors, Interfaces.Node currentNode,
                                                    BiConsumer<List<Interfaces.Node>, Interfaces.Node> visit) {
            if (!visitedNodeIds.add(currentNode)) {
                return;
            }

            visit.accept(ancestors, currentNode);

            ancestors.add(currentNode);
            try {
                for (Iterator<? extends Interfaces.Node> it = currentNode.iterator(); it.hasNext(); ) {
                    depthFirstPreOrderFullTraversal(depth + 1, visitedNodeIds, ancestors, Optional.ofNullable(it.next()).orElseThrow(), visit);
                }
            } finally {
                ancestors.remove(currentNode);
            }
        }

        List<Interfaces.Node> createNodeList() {
            return new ArrayList<>();
        }

        @Override
        public void traverse(int depth, Set<Interfaces.Node> visitedNodeIds, List<Interfaces.Node> ancestors, BiConsumer<List<Interfaces.Node>, Interfaces.Node> visit) {
            depthFirstPreOrderFullTraversal(depth, visitedNodeIds, ancestors, this, visit);
        }

        @Override
        public String toPrintableExecutionPlan() {
            return PrintableExecutionPlan.build(this);
        }
    }

    abstract static class FlowNodeWithId extends FlowNode implements Interfaces.NodeWithId {
        final String id;

        FlowNodeWithId(String id) {
            this.id = id;
        }

        @Override
        public String id() {
            return id;
        }
    }

    abstract static class OperationNode extends FlowNodeWithId implements Interfaces.Operation {
        OperationNode(String id) {
            super(id);
        }
    }

    static class GetNode extends OperationNode implements Interfaces.Get {

        final String url;
        final List<Interfaces.Node> steps;
        final List<String> returnVariables;

        GetNode(String id, String url, List<Interfaces.Node> steps, List<String> returnVariables) {
            super(id);
            this.url = url;
            this.steps = steps;
            this.returnVariables = returnVariables;
        }

        @Override
        public String url() {
            return url;
        }

        @Override
        public List<? extends Interfaces.Node> steps() {
            return steps;
        }

        @Override
        public List<String> returnVariables() {
            return returnVariables;
        }

        @Override
        public Iterator<? extends Interfaces.Node> iterator() {
            return steps.iterator();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GetNode getNode = (GetNode) o;
            return url.equals(getNode.url) &&
                    Objects.equals(steps, getNode.steps) &&
                    Objects.equals(returnVariables, getNode.returnVariables);
        }

        @Override
        public int hashCode() {
            return Objects.hash(url, steps, returnVariables);
        }

        @Override
        public String toString() {
            return "GetNode{" +
                    "url='" + url + '\'' +
                    ", steps=" + steps +
                    ", returnVariables=" + returnVariables +
                    '}';
        }
    }

    static class PaginateNode extends OperationNode implements Interfaces.Paginate {

        final Map<String, String> variables;
        final boolean addPageContent;
        final List<Interfaces.Execute> children;
        final double threshold;
        final ConditionNode conditionNode;

        PaginateNode(String id, Map<String, String> variables, boolean addPageContent, List<Interfaces.Execute> children, double threshold, ConditionNode conditionNode) {
            super(id);
            this.variables = variables;
            this.addPageContent = addPageContent;
            this.children = children;
            this.threshold = threshold;
            this.conditionNode = conditionNode;
        }

        @Override
        public Set<String> variableNames() {
            return variables.keySet();
        }

        @Override
        public String variable(String name) {
            return variables.get(name);
        }

        @Override
        public boolean addPageContent() {
            return addPageContent;
        }

        @Override
        public List<Interfaces.Execute> targets() {
            return children;
        }

        @Override
        public double threshold() {
            return threshold;
        }

        @Override
        public Interfaces.Condition condition() {
            return conditionNode;
        }

        @Override
        public Iterator<? extends Interfaces.Node> iterator() {
            return children.iterator();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PaginateNode that = (PaginateNode) o;
            return addPageContent == that.addPageContent &&
                    Double.compare(that.threshold, threshold) == 0 &&
                    Objects.equals(variables, that.variables) &&
                    Objects.equals(children, that.children) &&
                    Objects.equals(conditionNode, that.conditionNode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(variables, addPageContent, children, threshold, conditionNode);
        }

        @Override
        public String toString() {
            return "PaginateNode{" +
                    "id='" + id + '\'' +
                    ", variables=" + variables +
                    ", addPageContent=" + addPageContent +
                    ", children=" + children +
                    ", threshold=" + threshold +
                    ", conditionNode=" + conditionNode +
                    '}';
        }
    }

    static class SequenceNode extends FlowNode implements Interfaces.Sequence {

        final QueryNode splitNode;
        final QueryNode expectedNode;

        SequenceNode(QueryNode splitNode, QueryNode expectedNode) {
            this.splitNode = splitNode;
            this.expectedNode = expectedNode;
        }

        @Override
        public QueryNode splitToListQuery() {
            return splitNode;
        }

        @Override
        public QueryNode expectedQuery() {
            return expectedNode;
        }

        @Override
        public Iterator<? extends Interfaces.Node> iterator() {
            return createNodeList().iterator();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SequenceNode that = (SequenceNode) o;
            return Objects.equals(splitNode, that.splitNode) &&
                    Objects.equals(expectedNode, that.expectedNode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(splitNode, expectedNode);
        }

        @Override
        public String toString() {
            return "SequenceNode{" +
                    "splitNode=" + splitNode +
                    ", expectedNode=" + expectedNode +
                    '}';
        }
    }

    static class NextPageNode extends FlowNode implements Interfaces.NextPage {

        private final Map<String, QueryNode> queryNodeMap;

        public NextPageNode(Map<String, QueryNode> queryNodeMap) {
            this.queryNodeMap = queryNodeMap;
        }

        @Override
        public Map<String, Interfaces.Query> outputs() {
            return queryNodeMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> (Interfaces.Query) y, LinkedHashMap::new));
        }

        @Override
        public Iterator<? extends Interfaces.Node> iterator() {
            return createNodeList().iterator();
        }
    }

    static class ParallelNode extends FlowNode implements Interfaces.Parallel {

        final QueryNode splitQueryNode;
        final Map<String, QueryNode> variables;
        final List<Interfaces.Node> steps;
        final Interfaces.Publish publishNode;

        ParallelNode(QueryNode splitQueryNode, Map<String, QueryNode> variables, List<Interfaces.Node> steps, Interfaces.Publish publishNode) {
            this.splitQueryNode = splitQueryNode;
            this.variables = variables;
            this.steps = steps;
            this.publishNode = publishNode;
        }

        @Override
        public QueryNode splitQuery() {
            return splitQueryNode;
        }

        @Override
        public Set<String> variableNames() {
            return variables.keySet();
        }

        @Override
        public QueryNode variable(String name) {
            return variables.get(name);
        }

        @Override
        public List<Interfaces.Node> steps() {
            return steps;
        }

        @Override
        public Interfaces.Publish publish() {
            return publishNode;
        }

        @Override
        public Iterator<? extends Interfaces.Node> iterator() {
            return steps.iterator();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ParallelNode that = (ParallelNode) o;
            return Objects.equals(splitQueryNode, that.splitQueryNode) &&
                    Objects.equals(variables, that.variables) &&
                    Objects.equals(steps, that.steps) &&
                    Objects.equals(publishNode, that.publishNode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(splitQueryNode, variables, steps, publishNode);
        }

        @Override
        public String toString() {
            return "ParallelNode{" +
                    "splitBuilder=" + splitQueryNode +
                    ", variables=" + variables +
                    ", steps=" + steps +
                    ", publishBuilder=" + publishNode +
                    '}';
        }
    }

    static class ExecuteNode extends FlowNode implements Interfaces.Execute {

        final String executeId;
        final List<String> requiredInputs;
        final Map<String, QueryNode> inputVariables;
        final OperationNode targetNode;

        ExecuteNode(String executeId, List<String> requiredInputs, Map<String, QueryNode> inputVariables, OperationNode targetNode) {
            if (targetNode == null) {
                throw new IllegalArgumentException("adjacent executeNode is null");
            }
            this.executeId = executeId;
            this.requiredInputs = requiredInputs;
            this.inputVariables = inputVariables;
            this.targetNode = targetNode;
        }

        @Override
        public String executeId() {
            return executeId;
        }

        @Override
        public List<String> requiredInputs() {
            return requiredInputs;
        }

        @Override
        public Map<String, Interfaces.Query> inputVariable() {
            return inputVariables.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> (Interfaces.Query) y, LinkedHashMap::new));
        }

        @Override
        public Interfaces.Operation target() {
            return targetNode;
        }

        @Override
        public Iterator<? extends Interfaces.Node> iterator() {
            List<Interfaces.Node> nodeList = createNodeList();
            nodeList.add(targetNode);
            return nodeList.iterator();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ExecuteNode that = (ExecuteNode) o;
            return Objects.equals(executeId, that.executeId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(executeId);
        }

        @Override
        public String toString() {
            return "ExecuteNode{" +
                    "executeId='" + executeId + '\'' +
                    ", requiredInputs=" + requiredInputs +
                    ", inputVariables=" + inputVariables +
                    ", executeNode=" + targetNode +
                    '}';
        }
    }

    static class ProcessNode extends FlowNode implements Interfaces.Process {

        final Class<? extends Processor> processorClass;
        final Set<String> requiredOutputs;

        ProcessNode(Class<? extends Processor> processorClass, Set<String> requiredOutputs) {
            this.processorClass = processorClass;
            this.requiredOutputs = requiredOutputs;
        }

        @Override
        public Class<? extends Processor> processorClass() {
            return processorClass;
        }

        @Override
        public Set<String> requiredOutputs() {
            return requiredOutputs;
        }

        @Override
        public Iterator<? extends Interfaces.Node> iterator() {
            return createNodeList().iterator();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ProcessNode that = (ProcessNode) o;
            return Objects.equals(processorClass, that.processorClass) &&
                    Objects.equals(requiredOutputs, that.requiredOutputs);
        }

        @Override
        public int hashCode() {
            return Objects.hash(processorClass, requiredOutputs);
        }

        @Override
        public String toString() {
            return "ProcessNode{" +
                    "processorClass=" + processorClass +
                    ", requiredOutputs=" + requiredOutputs +
                    '}';
        }
    }

    static class AddContentNode extends FlowNode implements Interfaces.AddContent {

        final String positionVariableExpression;
        final String contentKey;

        AddContentNode(String positionVariableExpression, String contentKey) {
            this.positionVariableExpression = positionVariableExpression;
            this.contentKey = contentKey;
        }

        @Override
        public String positionVariableExpression() {
            return positionVariableExpression;
        }

        @Override
        public String contentKey() {
            return contentKey;
        }

        @Override
        public Iterator<? extends Interfaces.Node> iterator() {
            return createNodeList().iterator();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AddContentNode that = (AddContentNode) o;
            return positionVariableExpression.equals(that.positionVariableExpression) &&
                    contentKey.equals(that.contentKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(positionVariableExpression, contentKey);
        }

        @Override
        public String toString() {
            return "AddContentNode{" +
                    "positionVariableExpression='" + positionVariableExpression + '\'' +
                    ", contentKey='" + contentKey + '\'' +
                    '}';
        }
    }

    static class PublishNode extends FlowNode implements Interfaces.Publish {

        final String positionVariableExpression;

        PublishNode(String positionVariableExpression) {
            this.positionVariableExpression = positionVariableExpression;
        }

        @Override
        public String positionVariableExpression() {
            return positionVariableExpression;
        }

        @Override
        public Iterator<? extends Interfaces.Node> iterator() {
            return createNodeList().iterator();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PublishNode that = (PublishNode) o;
            return Objects.equals(positionVariableExpression, that.positionVariableExpression);
        }

        @Override
        public int hashCode() {
            return Objects.hash(positionVariableExpression);
        }

        @Override
        public String toString() {
            return "PublishNode{" +
                    "positionVariableExpression='" + positionVariableExpression + '\'' +
                    '}';
        }

    }

    abstract static class QueryNode extends AbstractBaseNode implements Interfaces.Query {
    }

    abstract static class ConditionNode extends AbstractBaseNode implements Interfaces.Condition {
    }


    static class EvalNode extends QueryNode implements Interfaces.Eval {

        final Interfaces.Query query;
        final String bindToVariable;
        final String expression;

        EvalNode(Interfaces.Query query, String bindToVariable, String expression) {

            this.query = query;
            this.bindToVariable = bindToVariable;
            this.expression = expression;
        }

        @Override
        public Interfaces.Query query() {
            return query;
        }

        @Override
        public String bind() {
            return bindToVariable;
        }

        @Override
        public String expression() {
            return expression;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EvalNode evalNode = (EvalNode) o;
            return query.equals(evalNode.query) &&
                    bindToVariable.equals(evalNode.bindToVariable) &&
                    expression.equals(evalNode.expression);
        }

        @Override
        public int hashCode() {
            return Objects.hash(query, bindToVariable, expression);
        }

        @Override
        public String toString() {
            return "EvalNode{" +
                    "query=" + query +
                    ", bindToVariable='" + bindToVariable + '\'' +
                    ", expression='" + expression + '\'' +
                    '}';
        }
    }

    static class XPathNode extends QueryNode implements Interfaces.XPath {

        final String expression;

        XPathNode(String expression) {
            this.expression = expression;
        }

        @Override
        public String expression() {
            return expression;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            XPathNode xPathNode = (XPathNode) o;
            return expression.equals(xPathNode.expression);
        }

        @Override
        public int hashCode() {
            return Objects.hash(expression);
        }

        @Override
        public String toString() {
            return "XPathNode{" +
                    "expression='" + expression + '\'' +
                    '}';
        }
    }

    static class RegExNode extends QueryNode implements Interfaces.RegEx {

        private final QueryNode queryNode;

        private final String expression;

        public RegExNode(QueryNode queryNode, String expression) {
            this.queryNode = queryNode;
            this.expression = expression;
        }

        @Override
        public Interfaces.Query query() {
            return queryNode;
        }

        @Override
        public String expression() {
            return expression;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof RegExNode)) return false;
            RegExNode regExNode = (RegExNode) o;
            return Objects.equals(queryNode, regExNode.queryNode) &&
                    Objects.equals(expression, regExNode.expression);
        }

        @Override
        public int hashCode() {
            return Objects.hash(queryNode, expression);
        }
        @Override
        public String toString() {
            return "RegExNode{" +
                    "query=" + queryNode +
                    ", expression='" + expression + '\'' +
                    '}';
        }

    }
    static class WhenVariableIsNullNode extends ConditionNode implements Interfaces.WhenVariableIsNull {


        final String identifier;

        WhenVariableIsNullNode(String identifier) {
            this.identifier = identifier;
        }

        @Override
        public String identifier() {
            return identifier;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WhenVariableIsNullNode that = (WhenVariableIsNullNode) o;
            return Objects.equals(identifier, that.identifier);
        }

        @Override
        public int hashCode() {
            return Objects.hash(identifier);
        }

        @Override
        public String toString() {
            return "WhenVariableIsNullNode{" +
                    "identifier='" + identifier + '\'' +
                    '}';
        }
    }
}
