package no.ssb.dc.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.module.SimpleModule;
import no.ssb.dc.api.util.JacksonFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Flow {

    final String name;
    final Interfaces.Node startNode;
    final Map<String, Interfaces.Node> nodeById;

    private Flow(String name, Interfaces.Node startNode, Map<String, Interfaces.Node> nodeById) {
        this.name = name;
        this.startNode = startNode;
        this.nodeById = nodeById;
    }

    public static FlowBuilder start(String name, String startNodeId) {
        return new FlowBuilder(name, startNodeId);
    }

    public static <R extends AbstractNodeBuilder> R deserialize(String source, Class<R> builderClass) {
        try {
            ObjectMapper mapper = JacksonFactory.yamlInstance().objectMapper();
            SimpleModule module = new SimpleModule();
            module.addDeserializer(AbstractNodeBuilder.class, new NodeBuilderDeserializer());
            mapper.registerModule(module);
            return mapper.readValue(source, builderClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String name() {
        return name;
    }

    public Interfaces.Node startNode() {
        return startNode;
    }

    public static abstract class AbstractNodeBuilder {

        @JsonProperty final BuilderType type;

        AbstractNodeBuilder(BuilderType type) {
            this.type = type;
        }
    }

    @JsonDeserialize(using = NodeBuilderDeserializer.class)
    public static class FlowBuilder extends AbstractNodeBuilder {

        @JsonProperty String flowName;
        @JsonProperty String startNodeId;

        @JsonProperty("nodes") Map<String, NodeBuilder> nodeBuilderById = new LinkedHashMap<>();

        private FlowBuilder(String flowName, String startNodeId) {
            super(BuilderType.Flow);
            this.flowName = flowName;
            this.startNodeId = startNodeId;
        }

        public FlowBuilder node(NodeBuilder builder) {
            nodeBuilderById.put(builder.getId(), builder);
            return this;
        }

        public Flow end() {
            Map<String, Interfaces.Node> nodeInstanceById = new LinkedHashMap<>();

            // add child nodes recursively to nodeInstanceById map
            for (Map.Entry<String, NodeBuilder> entry : nodeBuilderById.entrySet()) {
                nodeInstanceById.put(entry.getKey(), entry.getValue().build(nodeBuilderById, nodeInstanceById));
            }

            return new Flow(flowName, nodeInstanceById.get(startNodeId), nodeInstanceById);
        }

        public NodeBuilder get(String nodeId) {
            return Optional.ofNullable(nodeBuilderById.get(nodeId)).orElseThrow();
        }

        public String serialize() {
            return JacksonFactory.yamlInstance().toPrettyJSON(this);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FlowBuilder)) return false;
            FlowBuilder builder = (FlowBuilder) o;
            return Objects.equals(flowName, builder.flowName) &&
                    Objects.equals(startNodeId, builder.startNodeId) &&
                    Objects.equals(nodeBuilderById, builder.nodeBuilderById);
        }

        @Override
        public int hashCode() {
            return Objects.hash(flowName, startNodeId, nodeBuilderById);
        }

        @Override
        public String toString() {
            return "FlowBuilder{" +
                    "flowName='" + flowName + '\'' +
                    ", startNodeId='" + startNodeId + '\'' +
                    ", nodeBuilderById=" + nodeBuilderById +
                    '}';
        }
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static abstract class NodeBuilder extends AbstractNodeBuilder {

        @JsonProperty String id;

        NodeBuilder(BuilderType type) {
            super(type);
        }

        String getId() {
            Objects.requireNonNull(id);
            return id;
        }

        void setId(String id) {
            this.id = id;
        }

        public String serialize() {
            return JacksonFactory.yamlInstance().toPrettyJSON(this);
        }

        /**
         * Successor is responsible for its own creation and must add itself to nodeInstanceById.
         * Lazy initialization is dont through nodeBuilderById.
         *
         * @param nodeBuilderById
         * @param nodeInstanceById
         * @return
         */
        abstract <R extends Interfaces.BaseNode> R build(Map<String, NodeBuilder> nodeBuilderById, Map<String, R> nodeInstanceById);

        public <R extends Interfaces.BaseNode> R build() {
            return build(new LinkedHashMap<>(), new LinkedHashMap<>());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof NodeBuilder)) return false;
            NodeBuilder that = (NodeBuilder) o;
            return type == that.type &&
                    Objects.equals(getId(), that.getId());
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, getId());
        }

        @Override
        public String toString() {
            return "NodeBuilder{" +
                    "type=" + type +
                    ", id='" + id + '\'' +
                    '}';
        }
    }

    public static abstract class OperationBuilder extends NodeBuilder {

        @JsonProperty String url;

        OperationBuilder(BuilderType type) {
            super(type);
        }
    }

    public static abstract class QueryBuilder extends NodeBuilder {

        QueryBuilder(BuilderType type) {
            super(type);
        }
    }

    public static abstract class ConditionBuilder extends NodeBuilder {

        ConditionBuilder(BuilderType type) {
            super(type);
        }
    }

    @JsonDeserialize(using = NodeBuilderDeserializer.class)
    public static class GetBuilder extends OperationBuilder {

        @JsonProperty List<NodeBuilder> steps = new ArrayList<>();
        @JsonProperty List<String> returnVariables = new ArrayList<>();

        GetBuilder() {
            super(BuilderType.Get);
        }

        GetBuilder(String id) {
            super(BuilderType.Get);
            setId(id);
        }

        public GetBuilder id(String id) {
            setId(id);
            return this;
        }

        public GetBuilder url(String urlString) {
            this.url = urlString;
            return this;
        }

        public GetBuilder step(NodeBuilder builder) {
            steps.add(builder);
            return this;
        }

        public GetBuilder returnVariables(String... variableKeys) {
            for (String variableKey : variableKeys) {
                returnVariables.add(variableKey);
            }
            return this;
        }

        @SuppressWarnings("unchecked")
        @Override
        <R extends Interfaces.BaseNode> R build(Map<String, NodeBuilder> nodeBuilderById, Map<String, R> nodeInstanceById) {
            List<Interfaces.Node> stepNodeList = new ArrayList<>();
            for (NodeBuilder stepBuilder : steps) {
                Interfaces.Node stepNode = (Interfaces.Node) stepBuilder.build(nodeBuilderById, nodeInstanceById);
                stepNodeList.add(stepNode);
            }
            return (R) new Nodes.GetNode(getId(), url, stepNodeList, returnVariables);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            GetBuilder that = (GetBuilder) o;
            return Objects.equals(steps, that.steps) &&
                    Objects.equals(returnVariables, that.returnVariables);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), steps, returnVariables);
        }

        @Override
        public String toString() {
            return "GetBuilder{" +
                    "id='" + id + '\'' +
                    ", url='" + url + '\'' +
                    ", steps=" + steps +
                    '}';
        }
    }

    @JsonDeserialize(using = NodeBuilderDeserializer.class)
    public static class PaginateBuilder extends OperationBuilder {

        @JsonProperty Map<String, String> variables = new LinkedHashMap<>();
        @JsonProperty List<ExecuteBuilder> children = new ArrayList<>();
        @JsonProperty double threshold;
        @JsonProperty("until") ConditionBuilder conditionBuilder;
        @JsonProperty boolean addPageContent;

        PaginateBuilder() {
            super(BuilderType.Paginate);
        }

        PaginateBuilder(String id) {
            super(BuilderType.Paginate);
            setId(id);
        }

        public PaginateBuilder variable(String identifier, String expression) {
            variables.put(identifier, expression);
            return this;
        }

        public PaginateBuilder step(ExecuteBuilder executeBuilder) {
            children.add(executeBuilder);
            return this;
        }

        public PaginateBuilder prefetchThreshold(double threshold) {
            this.threshold = threshold;
            return this;
        }

        public PaginateBuilder until(ConditionBuilder conditionBuilder) {
            this.conditionBuilder = conditionBuilder;
            return this;
        }

        public PaginateBuilder addPageContent() {
            this.addPageContent = true;
            return this;
        }

        @SuppressWarnings("unchecked")
        @Override
        <R extends Interfaces.BaseNode> R build(Map<String, NodeBuilder> nodeBuilderById, Map<String, R> nodeInstanceById) {
            List<Interfaces.Execute> executeNodeList = new ArrayList<>();

            for (ExecuteBuilder executeBuilder : children) {
                Interfaces.Execute executeNode = (Interfaces.Execute) executeBuilder.build(nodeBuilderById, nodeInstanceById);
                executeNodeList.add(executeNode);
            }

            Nodes.ConditionNode conditionNode = (Nodes.ConditionNode) conditionBuilder.build(nodeBuilderById, nodeInstanceById);

            return (R) new Nodes.PaginateNode(getId(), variables, addPageContent, executeNodeList, threshold, conditionNode);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            PaginateBuilder builder = (PaginateBuilder) o;
            return Double.compare(builder.threshold, threshold) == 0 &&
                    addPageContent == builder.addPageContent &&
                    Objects.equals(variables, builder.variables) &&
                    Objects.equals(children, builder.children) &&
                    Objects.equals(conditionBuilder, builder.conditionBuilder);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), variables, children, threshold, conditionBuilder, addPageContent);
        }

        @Override
        public String toString() {
            return "PaginateBuilder{" +
                    "id='" + id + '\'' +
                    ", variables=" + variables +
                    ", children=" + children +
                    ", threshold=" + threshold +
                    ", conditionBuilder=" + conditionBuilder +
                    ", addPageContent=" + addPageContent +
                    '}';
        }
    }

    @JsonDeserialize(using = NodeBuilderDeserializer.class)
    public static class SequenceBuilder extends NodeBuilder {

        @JsonProperty("splitQuery") QueryBuilder splitBuilder;
        @JsonProperty("expectedQuery") QueryBuilder expectedBuilder;

        SequenceBuilder(QueryBuilder splitBuilder) {
            super(BuilderType.Sequence);
            this.splitBuilder = splitBuilder;
        }

        void splitBuilder(QueryBuilder splitBuilder) {
            this.splitBuilder = splitBuilder;
        }

        public SequenceBuilder expected(QueryBuilder expectedBuilder) {
            this.expectedBuilder = expectedBuilder;
            return this;
        }

        @SuppressWarnings("unchecked")
        @Override
        <R extends Interfaces.BaseNode> R build(Map<String, NodeBuilder> nodeBuilderById, Map<String, R> nodeInstanceById) {
            Nodes.QueryNode splitToListQueryNode = (Nodes.QueryNode) splitBuilder.build(nodeBuilderById, nodeInstanceById);
            Nodes.QueryNode splitCriteriaQueryNode = (Nodes.QueryNode) expectedBuilder.build(nodeBuilderById, nodeInstanceById);
            return (R) new Nodes.SequenceNode(splitToListQueryNode, splitCriteriaQueryNode);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SequenceBuilder)) return false;
            SequenceBuilder that = (SequenceBuilder) o;
            return Objects.equals(splitBuilder, that.splitBuilder) &&
                    Objects.equals(expectedBuilder, that.expectedBuilder);
        }

        @Override
        public int hashCode() {
            return Objects.hash(splitBuilder, expectedBuilder);
        }

        @Override
        public String toString() {
            return "SequenceBuilder{" +
                    "splitBuilder=" + splitBuilder +
                    ", expectedBuilder=" + expectedBuilder +
                    '}';
        }
    }

    @JsonDeserialize(using = NodeBuilderDeserializer.class)
    public static class NextPageBuilder extends NodeBuilder {

        @JsonProperty("outputs") Map<String, QueryBuilder> outputMap = new LinkedHashMap<>();

        NextPageBuilder() {
            super(BuilderType.NextPage);
        }

        public NextPageBuilder output(String variable, QueryBuilder queryBuilder) {
            outputMap.put(variable, queryBuilder);
            return this;
        }

        @Override
        <R extends Interfaces.BaseNode> R build(Map<String, NodeBuilder> nodeBuilderById, Map<String, R> nodeInstanceById) {
            Map<String, Nodes.QueryNode> queryNodeMap = new LinkedHashMap<>();
            for (Map.Entry<String, QueryBuilder> entry : outputMap.entrySet()) {
                queryNodeMap.put(entry.getKey(), (Nodes.QueryNode) entry.getValue().build(nodeBuilderById, nodeInstanceById));
            }
            return (R) new Nodes.NextPageNode(queryNodeMap);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof NextPageBuilder)) return false;
            NextPageBuilder builder = (NextPageBuilder) o;
            return Objects.equals(outputMap, builder.outputMap);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), outputMap);
        }

        @Override
        public String toString() {
            return "NextPageBuilder{" +
                    "outputMap=" + outputMap +
                    '}';
        }
    }

    @JsonDeserialize(using = NodeBuilderDeserializer.class)
    public static class ParallelBuilder extends NodeBuilder {

        @JsonProperty("splitQuery") QueryBuilder splitBuilder;
        @JsonProperty Map<String, QueryBuilder> variables = new LinkedHashMap<>();
        @JsonProperty List<NodeBuilder> steps = new ArrayList<>();
        @JsonProperty("publish") PublishBuilder publishBuilder;

        ParallelBuilder(QueryBuilder splitBuilder) {
            super(BuilderType.Parallel);
            this.splitBuilder = splitBuilder;
        }

        public ParallelBuilder variable(String identifier, QueryBuilder queryBuilder) {
            variables.put(identifier, queryBuilder);
            return this;
        }

        public ParallelBuilder step(NodeBuilder builder) {
            steps.add(builder);
            return this;
        }

        ParallelBuilder publish(PublishBuilder publishBuilder) {
            this.publishBuilder = publishBuilder;
            return this;
        }

        @SuppressWarnings("unchecked")
        @Override
        <R extends Interfaces.BaseNode> R build(Map<String, NodeBuilder> nodeBuilderById, Map<String, R> nodeInstanceById) {
            Nodes.QueryNode splitToListQueryNode = (Nodes.QueryNode) splitBuilder.build(nodeBuilderById, nodeInstanceById);

            Map<String, Nodes.QueryNode> contextVariablesMap = new LinkedHashMap<>();
            for (Map.Entry<String, QueryBuilder> entry : variables.entrySet()) {
                Nodes.QueryNode node = (Nodes.QueryNode) entry.getValue().build(nodeBuilderById, nodeInstanceById);
                contextVariablesMap.put(entry.getKey(), node);
            }

            List<Interfaces.Node> stepList = new ArrayList<>();
            for (NodeBuilder builder : steps) {
                Interfaces.Node node = (Interfaces.Node) builder.build(nodeBuilderById, nodeInstanceById);
                stepList.add(node);
            }

            Nodes.PublishNode publishNode = publishBuilder == null ? null : (Nodes.PublishNode) publishBuilder.build(nodeBuilderById, nodeInstanceById);

            return (R) new Nodes.ParallelNode(splitToListQueryNode, contextVariablesMap, stepList, publishNode);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ParallelBuilder)) return false;
            ParallelBuilder that = (ParallelBuilder) o;
            return Objects.equals(splitBuilder, that.splitBuilder) &&
                    Objects.equals(variables, that.variables) &&
                    Objects.equals(steps, that.steps) &&
                    Objects.equals(publishBuilder, that.publishBuilder);
        }

        @Override
        public int hashCode() {
            return Objects.hash(splitBuilder, variables, steps, publishBuilder);
        }

        @Override
        public String toString() {
            return "ParallelBuilder{" +
                    "splitBuilder=" + splitBuilder +
                    ", variables=" + variables +
                    ", steps=" + steps +
                    ", publishBuilder=" + publishBuilder +
                    '}';
        }
    }

    @JsonDeserialize(using = NodeBuilderDeserializer.class)
    public static class ExecuteBuilder extends NodeBuilder {

        @JsonProperty String executeId;
        @JsonProperty List<String> requiredInputs = new ArrayList<>();
        @JsonProperty Map<String, QueryBuilder> inputVariables = new LinkedHashMap<>();

        ExecuteBuilder(String executeId) {
            super(BuilderType.Execute);
            this.executeId = executeId;
        }

        public ExecuteBuilder requiredInput(String identifier) {
            requiredInputs.add(identifier);
            return this;
        }

        public ExecuteBuilder inputVariable(String identifier, QueryBuilder queryBuilder) {
            inputVariables.put(identifier, queryBuilder);
            return this;
        }

        @SuppressWarnings("unchecked")
        @Override
        <R extends Interfaces.BaseNode> R build(Map<String, NodeBuilder> nodeBuilderById, Map<String, R> nodeInstanceById) {
            Map<String, Nodes.QueryNode> inputVariableMap = inputVariables.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> (Nodes.QueryNode) e.getValue().build(nodeBuilderById, nodeInstanceById)));

            if (!nodeBuilderById.containsKey(executeId)) {
                throw new RuntimeException("Builder" + this.getClass() + " points to an undefined node: " + this.executeId);
            }

            Nodes.OperationNode targetExecuteNode = (Nodes.OperationNode) (nodeInstanceById.containsKey(executeId) ?
                    nodeInstanceById.get(executeId) :
                    nodeBuilderById.get(executeId).build(nodeBuilderById, nodeInstanceById));

            nodeInstanceById.computeIfAbsent(executeId, node -> (R) targetExecuteNode);

            return (R) new Nodes.ExecuteNode(executeId, requiredInputs, inputVariableMap, targetExecuteNode);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ExecuteBuilder)) return false;
            ExecuteBuilder that = (ExecuteBuilder) o;
            return Objects.equals(executeId, that.executeId) &&
                    Objects.equals(requiredInputs, that.requiredInputs) &&
                    Objects.equals(inputVariables, that.inputVariables);
        }

        @Override
        public int hashCode() {
            return Objects.hash(executeId, requiredInputs, inputVariables);
        }

        @Override
        public String toString() {
            return "ExecuteBuilder{" +
                    "executeId='" + executeId + '\'' +
                    ", requiredInputs=" + requiredInputs +
                    ", inputVariables=" + inputVariables +
                    '}';
        }
    }

    @JsonDeserialize(using = NodeBuilderDeserializer.class)
    public static class ProcessBuilder extends NodeBuilder {

        @JsonProperty Class<? extends Processor> processorClass;
        @JsonProperty Set<String> requiredOutputs = new LinkedHashSet<>();

        ProcessBuilder(Class<? extends Processor> processorClass) {
            super(BuilderType.Process);
            this.processorClass = processorClass;
        }

        public ProcessBuilder output(String variable) {
            requiredOutputs.add(variable);
            return this;
        }

        @SuppressWarnings("unchecked")
        @Override
        <R extends Interfaces.BaseNode> R build(Map<String, NodeBuilder> nodeBuilderById, Map<String, R> nodeInstanceById) {
            return (R) new Nodes.ProcessNode(processorClass, requiredOutputs);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ProcessBuilder)) return false;
            ProcessBuilder that = (ProcessBuilder) o;
            return Objects.equals(processorClass, that.processorClass) &&
                    Objects.equals(requiredOutputs, that.requiredOutputs);
        }

        @Override
        public int hashCode() {
            return Objects.hash(processorClass, requiredOutputs);
        }

        @Override
        public String toString() {
            return "ProcessBuilder{" +
                    "processorClass=" + processorClass +
                    ", requiredOutputs=" + requiredOutputs +
                    '}';
        }
    }

    @JsonDeserialize(using = NodeBuilderDeserializer.class)
    public static class AddContentBuilder extends NodeBuilder {

        @JsonProperty String positionVariableExpression;

        @JsonProperty String contentKey;

        AddContentBuilder(String positionVariableExpression, String contentKey) {
            super(BuilderType.AddContent);
            this.positionVariableExpression = positionVariableExpression;
            this.contentKey = contentKey;
        }

        @SuppressWarnings("unchecked")
        @Override
        <R extends Interfaces.BaseNode> R build(Map<String, NodeBuilder> nodeBuilderById, Map<String, R> nodeInstanceById) {
            return (R) new Nodes.AddContentNode(positionVariableExpression, contentKey);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AddContentBuilder that = (AddContentBuilder) o;
            return positionVariableExpression.equals(that.positionVariableExpression) &&
                    contentKey.equals(that.contentKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), positionVariableExpression, contentKey);
        }

        @Override
        public String toString() {
            return "AddContentBuilder{" +
                    "positionVariableExpression='" + positionVariableExpression + '\'' +
                    ", contentKey='" + contentKey + '\'' +
                    '}';
        }
    }

    @JsonDeserialize(using = NodeBuilderDeserializer.class)
    public static class PublishBuilder extends NodeBuilder {

        @JsonProperty String positionVariableExpression;

        PublishBuilder(String positionVariableExpression) {
            super(BuilderType.Publish);
            this.positionVariableExpression = positionVariableExpression;
        }

        @SuppressWarnings("unchecked")
        @Override
        <R extends Interfaces.BaseNode> R build(Map<String, NodeBuilder> nodeBuilderById, Map<String, R> nodeInstanceById) {
            return (R) new Nodes.PublishNode(positionVariableExpression);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PublishBuilder)) return false;
            PublishBuilder that = (PublishBuilder) o;
            return Objects.equals(positionVariableExpression, that.positionVariableExpression);
        }

        @Override
        public int hashCode() {
            return Objects.hash(positionVariableExpression);
        }

        @Override
        public String toString() {
            return "PublishBuilder{" +
                    "positionVariableExpression='" + positionVariableExpression + '\'' +
                    '}';
        }
    }

    @JsonDeserialize(using = NodeBuilderDeserializer.class)
    public static class EvalBuilder extends QueryBuilder {

        @JsonProperty("query") QueryBuilder queryBuilder;
        @JsonProperty("bindToVariable") String bindToVariable;
        @JsonProperty("elExpression") String elExpression;

        EvalBuilder(QueryBuilder queryBuilder, String bindToVariable, String elExpression) {
            super(BuilderType.QueryEval);
            this.queryBuilder = queryBuilder;
            this.bindToVariable = bindToVariable;
            this.elExpression = elExpression;
        }


        @Override
        <R extends Interfaces.BaseNode> R build(Map<String, NodeBuilder> nodeBuilderById, Map<String, R> nodeInstanceById) {
            Nodes.QueryNode queryNode = (Nodes.QueryNode) queryBuilder.build(nodeBuilderById, nodeInstanceById);
            return (R) new Nodes.EvalNode(queryNode, bindToVariable, elExpression);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            EvalBuilder that = (EvalBuilder) o;
            return queryBuilder.equals(that.queryBuilder) &&
                    bindToVariable.equals(that.bindToVariable) &&
                    elExpression.equals(that.elExpression);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), queryBuilder, bindToVariable, elExpression);
        }
    }


    @JsonDeserialize(using = NodeBuilderDeserializer.class)
    public static class XPathBuilder extends QueryBuilder {

        @JsonProperty String expression;

        XPathBuilder(String expression) {
            super(BuilderType.QueryXPath);
            this.expression = expression;
        }

        @SuppressWarnings("unchecked")
        @Override
        <R extends Interfaces.BaseNode> R build(Map<String, NodeBuilder> nodeBuilderById, Map<String, R> nodeInstanceById) {
            return (R) new Nodes.XPathNode(expression);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            XPathBuilder that = (XPathBuilder) o;
            return expression.equals(that.expression);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), expression);
        }

        @Override
        public String toString() {
            return "XPathBuilder{" +
                    "expression='" + expression + '\'' +
                    '}';
        }
    }

    @JsonDeserialize(using = NodeBuilderDeserializer.class)
    public static class RegExBuilder extends QueryBuilder {

        @JsonProperty("query") QueryBuilder queryBuilder;
        @JsonProperty String expression;

        RegExBuilder(QueryBuilder queryBuilder, String expression) {
            super(BuilderType.QueryRegEx);
            this.queryBuilder = queryBuilder;
            this.expression = expression;
        }

        @Override
        <R extends Interfaces.BaseNode> R build(Map<String, NodeBuilder> nodeBuilderById, Map<String, R> nodeInstanceById) {
            Nodes.QueryNode queryNode = (Nodes.QueryNode) queryBuilder.build(nodeBuilderById, nodeInstanceById);
            return (R) new Nodes.RegExNode(queryNode, expression);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof RegExBuilder)) return false;
            RegExBuilder that = (RegExBuilder) o;
            return Objects.equals(queryBuilder, that.queryBuilder) &&
                    Objects.equals(expression, that.expression);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), queryBuilder, expression);
        }

        @Override
        public String toString() {
            return "RegExBuilder{" +
                    "queryBuilder=" + queryBuilder +
                    ", expression='" + expression + '\'' +
                    '}';
        }
    }

    @JsonDeserialize(using = NodeBuilderDeserializer.class)
    public static class WhenVariableIsNullBuilder extends ConditionBuilder {

        @JsonProperty String identifier;

        WhenVariableIsNullBuilder() {
            super(BuilderType.ConditionWhenVariableIsNull);
        }

        public WhenVariableIsNullBuilder identifier(String identifier) {
            this.identifier = identifier;
            return this;
        }

        @SuppressWarnings("unchecked")
        @Override
        <R extends Interfaces.BaseNode> R build(Map<String, NodeBuilder> nodeBuilderById, Map<String, R> nodeInstanceById) {
            return (R) new Nodes.WhenVariableIsNullNode(identifier);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof WhenVariableIsNullBuilder)) return false;
            WhenVariableIsNullBuilder that = (WhenVariableIsNullBuilder) o;
            return Objects.equals(identifier, that.identifier);
        }

        @Override
        public int hashCode() {
            return Objects.hash(identifier);
        }

        @Override
        public String toString() {
            return "WhenVariableIsNullBuilder{" +
                    "identifier='" + identifier + '\'' +
                    '}';
        }
    }

}
