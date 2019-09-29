package no.ssb.dc.api;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * TODO Rewrite to a generic deserializer
 */
public class NodeBuilderDeserializer extends StdDeserializer<Flow.AbstractNodeBuilder> {

    public NodeBuilderDeserializer() {
        this(null);
    }

    protected NodeBuilderDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Flow.AbstractNodeBuilder deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        return handleNodeBuilder(0, context, new LinkedList<>(), null, node);
    }

    private String nodePath(Deque<JsonNode> ancestors) {
        return "/" + ancestors.stream().filter(node -> node != null).map(node -> {
            String id = node.has("id") ? node.get("id").textValue() : null;
            String type = node.has("type") ? node.get("type").textValue() : null;
            return String.format("%s%s", type, id == null ? "" : String.format("[%s]", id));
        }).collect(Collectors.joining("/"));
    }

    private Flow.AbstractNodeBuilder handleNodeBuilder(int depth, DeserializationContext context, Deque<JsonNode> ancestors,
                                                       JsonNode previousNode, JsonNode currentNode) {
        if (currentNode == null || !currentNode.has("type")) {
            throw new RuntimeException(String.format("Error resolving node: %s => %s", nodePath(ancestors), ancestors.peekLast()));
        }
        BuilderType type = BuilderType.parse(currentNode.get("type").textValue());

        ancestors.add(previousNode);
        Flow.AbstractNodeBuilder nextNodeBuilder = buildNodeBuilder(depth, context, ancestors, currentNode, type);
        ancestors.remove(previousNode);
        return nextNodeBuilder;
    }

    private Flow.AbstractNodeBuilder buildNodeBuilder(int depth, DeserializationContext context, Deque<JsonNode> ancestors, JsonNode currentNode, BuilderType type) {
        Objects.requireNonNull(type);
        switch (type) {
            case Flow: {
                Flow.FlowBuilder builder = Flow.start(currentNode.get("flowName").textValue(), currentNode.get("startNodeId").textValue());

                currentNode.get("nodes").fields().forEachRemaining(entry ->
                        builder.node((Flow.NodeBuilder) handleNodeBuilder(depth + 1, context, ancestors, currentNode, entry.getValue())));

                return builder;
            }

            case Paginate: {
                Flow.PaginateBuilder builder = new Flow.PaginateBuilder(currentNode.get("id").textValue());

                currentNode.get("variables").fields().forEachRemaining(entry -> builder.variable(entry.getKey(), entry.getValue().textValue()));

                if (currentNode.has("addPageContent") && currentNode.get("addPageContent").asBoolean()) {
                    builder.addPageContent();
                }

                currentNode.get("children").forEach(child -> builder.step((Flow.ExecuteBuilder) handleNodeBuilder(depth + 1, context, ancestors, currentNode, child)));

                JsonNode thresholdNode = currentNode.get("threshold");
                builder.prefetchThreshold(thresholdNode.asDouble());

                JsonNode conditionBuilderNode = currentNode.get("until");
                builder.until((Flow.ConditionBuilder) handleNodeBuilder(depth + 1, context, ancestors, currentNode, conditionBuilderNode));

                return builder;
            }

            case Sequence: {
                Flow.SequenceBuilder builder = new Flow.SequenceBuilder(null);

                Flow.QueryBuilder splitBuilder = (Flow.QueryBuilder) handleNodeBuilder(depth + 1, context, ancestors, currentNode, currentNode.get("splitQuery"));
                builder.splitBuilder(splitBuilder);

                Flow.QueryBuilder expectedBuilder = (Flow.QueryBuilder) handleNodeBuilder(depth + 1, context, ancestors, currentNode, currentNode.get("expectedQuery"));
                builder.expected(expectedBuilder);

                return builder;
            }

            case NextPage: {
                Flow.NextPageBuilder builder = new Flow.NextPageBuilder();

                currentNode.get("outputs").fields().forEachRemaining(entry ->
                        builder.output(
                                entry.getKey(),
                                (Flow.QueryBuilder) handleNodeBuilder(depth + 1, context, ancestors, currentNode, entry.getValue())
                        )
                );

                return builder;
            }

            case Parallel: {
                Flow.QueryBuilder splitBuilder = (Flow.QueryBuilder) handleNodeBuilder(depth + 1, context, ancestors, currentNode, currentNode.get("splitQuery"));
                Flow.ParallelBuilder builder = new Flow.ParallelBuilder(splitBuilder);

                currentNode.get("variables").fields().forEachRemaining(entry ->
                        builder.variable(entry.getKey(), (Flow.QueryBuilder) handleNodeBuilder(depth + 1, context, ancestors, currentNode, entry.getValue())));

                JsonNode stepsNode = currentNode.get("steps");
                stepsNode.forEach(stepNode -> builder.step((Flow.NodeBuilder) handleNodeBuilder(depth + 1, context, ancestors, currentNode, stepNode)));

                JsonNode publishJsonNode = currentNode.get("publish");
                if (publishJsonNode != null) {
                    Flow.PublishBuilder publishBuilder = (Flow.PublishBuilder) handleNodeBuilder(depth + 1, context, ancestors, currentNode, publishJsonNode);
                    builder.publish(publishBuilder);
                }

                return builder;
            }

            case Execute: {
                Flow.ExecuteBuilder builder = new Flow.ExecuteBuilder(currentNode.get("executeId").textValue());

                if (currentNode.has("requiredInputs")) {
                    currentNode.get("requiredInputs").forEach(requiredInput -> builder.requiredInput(requiredInput.textValue()));
                }

                if (currentNode.has("inputVariables")) {
                    currentNode.get("inputVariables").fields().forEachRemaining(entry ->
                            builder.inputVariable(entry.getKey(), (Flow.QueryBuilder) handleNodeBuilder(depth + 1, context, ancestors, currentNode, entry.getValue())));
                }
                return builder;
            }

            case Process: {
                try {
                    Flow.ProcessBuilder builder = new Flow.ProcessBuilder((Class<? extends Processor>) Class.forName(currentNode.get("processorClass").textValue()));
                    currentNode.get("requiredOutputs").forEach(output -> builder.output(output.textValue()));
                    return builder;
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }

            case QueryEval: {
                Flow.QueryBuilder queryBuilder = (Flow.QueryBuilder) handleNodeBuilder(depth + 1, context, ancestors, currentNode, currentNode.get("query"));
                String bindToVariableNode = currentNode.get("bindToVariable").textValue();
                String expression = currentNode.get("expression").textValue();
                return new Flow.EvalBuilder(queryBuilder, bindToVariableNode, expression);
            }

            case QueryXPath: {
                return new Flow.XPathBuilder(currentNode.get("expression").textValue());
            }

            case QueryRegEx: {
                return new Flow.RegExBuilder((Flow.QueryBuilder) handleNodeBuilder(depth + 1, context, ancestors, currentNode, currentNode.get("query")), currentNode.get("expression").textValue());
            }

            case ConditionWhenVariableIsNull: {
                Flow.WhenVariableIsNullBuilder builder = new Flow.WhenVariableIsNullBuilder();
                builder.identifier(currentNode.get("identifier").textValue());
                return builder;
            }

            case AddContent: {
                return new Flow.AddContentBuilder(currentNode.get("positionVariableExpression").textValue(), currentNode.get("contentKey").textValue());
            }

            case Publish: {
                return new Flow.PublishBuilder(currentNode.get("positionVariableExpression").textValue());
            }

            case Get: {
                Flow.GetBuilder builder = new Flow.GetBuilder(currentNode.get("id").textValue());

                builder.url(currentNode.get("url").textValue());

                ArrayNode returnVariablesNode = (ArrayNode) currentNode.get("returnVariables");
                if (returnVariablesNode != null) {
                    returnVariablesNode.forEach(node -> builder.returnVariables.add(node.textValue()));
                }

                JsonNode stepsNode = currentNode.get("steps");
                stepsNode.forEach(stepNode -> builder.step((Flow.NodeBuilder) handleNodeBuilder(depth + 1, context, ancestors, currentNode, stepNode)));

                return builder;
            }
        }

        throw new UnsupportedOperationException("NodeBuilder type '" + type + "' NOT supported!");
    }
}