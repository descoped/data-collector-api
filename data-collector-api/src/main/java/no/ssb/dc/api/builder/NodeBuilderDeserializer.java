package no.ssb.dc.api.builder;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import no.ssb.dc.api.Flow;
import no.ssb.dc.api.PositionProducer;
import no.ssb.dc.api.Processor;

import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * TODO Rewrite to a generic deserializer
 */
public class NodeBuilderDeserializer extends StdDeserializer<AbstractNodeBuilder> {

    public NodeBuilderDeserializer() {
        this(null);
    }

    protected NodeBuilderDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public AbstractNodeBuilder deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
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

    private AbstractNodeBuilder handleNodeBuilder(int depth, DeserializationContext context, Deque<JsonNode> ancestors,
                                                  JsonNode previousNode, JsonNode currentNode) {
        if (currentNode == null || !currentNode.has("type")) {
            throw new RuntimeException(String.format("Error resolving node: %s => %s", nodePath(ancestors), ancestors.peekLast()));
        }
        BuilderType type = BuilderType.parse(currentNode.get("type").textValue());

        ancestors.add(previousNode);
        AbstractNodeBuilder nextNodeBuilder = buildNodeBuilder(depth, context, ancestors, currentNode, type);
        ancestors.remove(previousNode);
        return nextNodeBuilder;
    }

    private AbstractNodeBuilder buildNodeBuilder(int depth, DeserializationContext context, Deque<JsonNode> ancestors, JsonNode currentNode, BuilderType type) {
        Objects.requireNonNull(type);
        switch (type) {
            case Flow: {
                FlowBuilder builder = Flow.start(currentNode.get("flowName").textValue(), currentNode.get("startNodeId").textValue());

                currentNode.get("nodes").fields().forEachRemaining(entry ->
                        builder.node((NodeBuilder) handleNodeBuilder(depth + 1, context, ancestors, currentNode, entry.getValue())));

                return builder;
            }

            case Paginate: {
                PaginateBuilder builder = new PaginateBuilder(currentNode.get("id").textValue());

                currentNode.get("variables").fields().forEachRemaining(entry -> builder.variable(entry.getKey(), entry.getValue().textValue()));

                if (currentNode.has("addPageContent") && currentNode.get("addPageContent").asBoolean()) {
                    builder.addPageContent();
                }

                currentNode.get("children").forEach(child -> builder.step((ExecuteBuilder) handleNodeBuilder(depth + 1, context, ancestors, currentNode, child)));

                JsonNode thresholdNode = currentNode.get("threshold");
                builder.prefetchThreshold(thresholdNode.asDouble());

                JsonNode conditionBuilderNode = currentNode.get("until");
                builder.until((ConditionBuilder) handleNodeBuilder(depth + 1, context, ancestors, currentNode, conditionBuilderNode));

                return builder;
            }

            case Sequence: {
                SequenceBuilder builder = new SequenceBuilder(null);

                QueryBuilder splitBuilder = (QueryBuilder) handleNodeBuilder(depth + 1, context, ancestors, currentNode, currentNode.get("splitQuery"));
                builder.splitBuilder(splitBuilder);

                QueryBuilder expectedBuilder = (QueryBuilder) handleNodeBuilder(depth + 1, context, ancestors, currentNode, currentNode.get("expectedQuery"));
                builder.expected(expectedBuilder);

                return builder;
            }

            case NextPage: {
                NextPageBuilder builder = new NextPageBuilder();

                currentNode.get("outputs").fields().forEachRemaining(entry ->
                        builder.output(
                                entry.getKey(),
                                (QueryBuilder) handleNodeBuilder(depth + 1, context, ancestors, currentNode, entry.getValue())
                        )
                );

                return builder;
            }

            case Parallel: {
                QueryBuilder splitBuilder = (QueryBuilder) handleNodeBuilder(depth + 1, context, ancestors, currentNode, currentNode.get("splitQuery"));
                ParallelBuilder builder = new ParallelBuilder(splitBuilder);

                currentNode.get("variables").fields().forEachRemaining(entry ->
                        builder.variable(entry.getKey(), (QueryBuilder) handleNodeBuilder(depth + 1, context, ancestors, currentNode, entry.getValue())));

                JsonNode stepsNode = currentNode.get("steps");
                stepsNode.forEach(stepNode -> builder.step((NodeBuilder) handleNodeBuilder(depth + 1, context, ancestors, currentNode, stepNode)));

                JsonNode publishJsonNode = currentNode.get("publish");
                if (publishJsonNode != null) {
                    PublishBuilder publishBuilder = (PublishBuilder) handleNodeBuilder(depth + 1, context, ancestors, currentNode, publishJsonNode);
                    builder.publish(publishBuilder);
                }

                return builder;
            }

            case Execute: {
                ExecuteBuilder builder = new ExecuteBuilder(currentNode.get("executeId").textValue());

                if (currentNode.has("requiredInputs")) {
                    currentNode.get("requiredInputs").forEach(requiredInput -> builder.requiredInput(requiredInput.textValue()));
                }

                if (currentNode.has("inputVariables")) {
                    currentNode.get("inputVariables").fields().forEachRemaining(entry ->
                            builder.inputVariable(entry.getKey(), (QueryBuilder) handleNodeBuilder(depth + 1, context, ancestors, currentNode, entry.getValue())));
                }
                return builder;
            }

            case Process: {
                try {
                    ProcessBuilder builder = new ProcessBuilder((Class<? extends Processor>) Class.forName(currentNode.get("processorClass").textValue()));
                    currentNode.get("requiredOutputs").forEach(output -> builder.output(output.textValue()));
                    return builder;
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }

            case QueryEval: {
                QueryBuilder queryBuilder = (QueryBuilder) handleNodeBuilder(depth + 1, context, ancestors, currentNode, currentNode.get("query"));
                String bindToVariableNode = currentNode.get("bindToVariable").textValue();
                String expression = currentNode.get("expression").textValue();
                return new EvalBuilder(queryBuilder, bindToVariableNode, expression);
            }

            case QueryXPath: {
                return new XPathBuilder(currentNode.get("expression").textValue());
            }

            case QueryRegEx: {
                return new RegExBuilder((QueryBuilder) handleNodeBuilder(depth + 1, context, ancestors, currentNode, currentNode.get("query")), currentNode.get("expression").textValue());
            }

            case ConditionWhenVariableIsNull: {
                WhenVariableIsNullBuilder builder = new WhenVariableIsNullBuilder();
                builder.identifier(currentNode.get("identifier").textValue());
                return builder;
            }

            case AddContent: {
                return new AddContentBuilder(currentNode.get("positionVariableExpression").textValue(), currentNode.get("contentKey").textValue());
            }

            case Publish: {
                return new PublishBuilder(currentNode.get("positionVariableExpression").textValue());
            }

            case Get: {
                GetBuilder builder = new GetBuilder(currentNode.get("id").textValue());

                builder.url(currentNode.get("url").textValue());

                if (currentNode.has("positionProducerClass")) builder.positionProducer(getPositionProducerClass(currentNode));

                ArrayNode returnVariablesNode = (ArrayNode) currentNode.get("returnVariables");
                if (returnVariablesNode != null) {
                    returnVariablesNode.forEach(node -> builder.returnVariables(node.textValue()));
                }

                JsonNode stepsNode = currentNode.get("steps");
                stepsNode.forEach(stepNode -> builder.step((NodeBuilder) handleNodeBuilder(depth + 1, context, ancestors, currentNode, stepNode)));

                return builder;
            }

            case ValidateRequest: {
                ValidateRequestBuilder builder = new ValidateRequestBuilder();

                return builder;
            }
        }

        throw new UnsupportedOperationException("NodeBuilder type '" + type + "' NOT supported!");
    }

    private Class<? extends PositionProducer> getPositionProducerClass(JsonNode currentNode) {
        try {
            return (Class<? extends PositionProducer>) Class.forName(currentNode.get("positionProducerClass").textValue());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}