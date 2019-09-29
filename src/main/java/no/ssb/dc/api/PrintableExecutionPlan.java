package no.ssb.dc.api;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * TODO imlement a specialized flow language (tbd) that expresses the flow in a human readable computer sequence
 */
class PrintableExecutionPlan {

    static String build(Interfaces.Node flowNode) {
        StringBuilder builder = new StringBuilder();
        Nodes.FlowNode.depthFirstPreOrderFullTraversal(0, new LinkedHashSet<>(), new LinkedList<>(), flowNode, (ancestors, visitNode) -> {
            String indent = Arrays.stream(new String[ancestors.size()]).map(element -> " ").collect(Collectors.joining());

            AtomicBoolean handled = new AtomicBoolean();

            visitNode.given(Nodes.GetNode.class, handled, node -> {
                builder.append(String.format("%s(%s) get(%s) => %s%n", indent, ancestors.size(),
                        node.id(),
                        node.url())
                );
            });

            visitNode.given(Nodes.ProcessNode.class, handled, node -> {
                builder.append(String.format("%s(%s) process(Class<%s>) produces required-variables: %s%n", indent, ancestors.size(),
                        node.processorClass().getSimpleName(),
                        node.requiredOutputs())
                );
            });

            visitNode.given(Nodes.ExecuteNode.class, handled, node -> {
                builder.append(String.format("%s(%s) execute(%s)", indent, ancestors.size(),
                        node.executeId()));

                if (!node.requiredInputs().isEmpty()) {
                    builder.append(" with required-input-variables: ").append(node.requiredInputs());
                }

                if (!node.inputVariable().isEmpty()) {
                    String inlineInputs = node.inputVariable().entrySet().stream().map(e -> String.format("%s with %s", e.getKey(), e.getValue().expression())).collect(Collectors.joining(", "));
                    builder.append(String.format(" bind [%s]", inlineInputs));
                }

                builder.append(String.format("%n"));
            });

            visitNode.given(Nodes.PaginateNode.class, handled, node -> {
                builder.append(String.format("%s(%s) paginate(%s)", indent, ancestors.size(),
                        node.id())
                );

                if (node.addPageContent) {
                    builder.append(" and addPageContent");
                }

                if (!node.variables.isEmpty()) {
                    String contextVariables = node.variables.entrySet().stream().map(e -> String.format("%s with %s", e.getKey(), e.getValue())).collect(Collectors.joining(", "));
                    builder.append(String.format(" bind [%s]", contextVariables));
                }

                builder.append(" until ").append(node.condition().getClass().getSimpleName()).append("(").append(node.condition().identifier()).append(")");

                builder.append(String.format("%n"));
            });

            visitNode.given(Nodes.SequenceNode.class, handled, node -> {
                builder.append(String.format("%s(%s) sequence forEach %s with expected-position %s%n", indent, ancestors.size(),
                        node.splitNode.expression(), node.expectedNode.expression()));
            });

            visitNode.given(Nodes.NextPageNode.class, handled, node -> {
                builder.append(String.format("%s(%s) nextPage output [%s]%n", indent, ancestors.size(),
                        node.outputs().entrySet().stream().map(entry -> (entry.getKey() + "=" + entry.getValue())).collect(Collectors.joining(",")) ));
            });

            visitNode.given(Nodes.ParallelNode.class, handled, node -> {
                builder.append(String.format("%s(%s) parallel each %s", indent, ancestors.size(),
                        node.splitQueryNode.expression())
                );

                if (!node.variables.isEmpty()) {
                    String contextVariables = node.variables.entrySet().stream()
                            .map(e -> String.format("%s with %s", e.getKey(), e.getValue().expression())).collect(Collectors.joining(", "));
                    builder.append(String.format(" bind [%s]", contextVariables));
                }

                builder.append(String.format("%n"));
            });

            visitNode.given(Nodes.AddContentNode.class, handled, node -> {
                builder.append(String.format("%s(%s) addContent to %s named '%s'%n", indent, ancestors.size(), node.positionVariableExpression(), node.contentKey()));
            });

            visitNode.given(Nodes.PublishNode.class, handled, node -> {
                builder.append(String.format("%s(%s) publish position by %s%n", indent, ancestors.size(),
                        node.positionVariableExpression)
                );
            });

            // serialize unhandled nodes
            if (!handled.get()) {
                builder.append(String.format("%s(%s) %s%n", indent, ancestors.size(), visitNode));
            }

        });
        return builder.toString();
    }
}
