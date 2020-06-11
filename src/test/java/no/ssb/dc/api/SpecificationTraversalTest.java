package no.ssb.dc.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.ssb.dc.api.node.builder.AbstractBuilder;
import no.ssb.dc.api.node.builder.AddContentBuilder;
import no.ssb.dc.api.node.builder.BodyContainsBuilder;
import no.ssb.dc.api.node.builder.BodyPublisherBuilder;
import no.ssb.dc.api.node.builder.BuilderType;
import no.ssb.dc.api.node.builder.DeleteBuilder;
import no.ssb.dc.api.node.builder.EvalBuilder;
import no.ssb.dc.api.node.builder.ExecuteBuilder;
import no.ssb.dc.api.node.builder.GetBuilder;
import no.ssb.dc.api.node.builder.HttpStatusValidationBuilder;
import no.ssb.dc.api.node.builder.JqPathBuilder;
import no.ssb.dc.api.node.builder.NextPageBuilder;
import no.ssb.dc.api.node.builder.PaginateBuilder;
import no.ssb.dc.api.node.builder.ParallelBuilder;
import no.ssb.dc.api.node.builder.PostBuilder;
import no.ssb.dc.api.node.builder.ProcessBuilder;
import no.ssb.dc.api.node.builder.PublishBuilder;
import no.ssb.dc.api.node.builder.PutBuilder;
import no.ssb.dc.api.node.builder.RegExBuilder;
import no.ssb.dc.api.node.builder.SecurityBuilder;
import no.ssb.dc.api.node.builder.SequenceBuilder;
import no.ssb.dc.api.node.builder.SpecificationBuilder;
import no.ssb.dc.api.node.builder.SpecificationContextBuilder;
import no.ssb.dc.api.node.builder.WhenExpressionIsTrueBuilder;
import no.ssb.dc.api.node.builder.WhenVariableIsNullBuilder;
import no.ssb.dc.api.node.builder.XPathBuilder;
import no.ssb.dc.api.util.JsonParser;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class SpecificationTraversalTest {

    static final Logger LOG = LoggerFactory.getLogger(SpecificationTraversalTest.class);
    static final ObjectMapper mapper = JsonParser.createJsonParser().mapper();

    static final Map<BuilderType, Class<? extends AbstractBuilder>> builders = new LinkedHashMap<>();

    static {
        builders.put(BuilderType.Specification, SpecificationBuilder.class);
        builders.put(BuilderType.SpecificationContext, SpecificationContextBuilder.class);
        builders.put(BuilderType.Security, SecurityBuilder.class);
        builders.put(BuilderType.Paginate, PaginateBuilder.class);
        builders.put(BuilderType.Sequence, SequenceBuilder.class);
        builders.put(BuilderType.NextPage, NextPageBuilder.class);
        builders.put(BuilderType.Parallel, ParallelBuilder.class);
        builders.put(BuilderType.Execute, ExecuteBuilder.class);
        builders.put(BuilderType.Process, ProcessBuilder.class);
        builders.put(BuilderType.QueryEval, EvalBuilder.class);
        builders.put(BuilderType.QueryXPath, XPathBuilder.class);
        builders.put(BuilderType.QueryJqPath, JqPathBuilder.class);
        builders.put(BuilderType.QueryRegEx, RegExBuilder.class);
        builders.put(BuilderType.ConditionWhenVariableIsNull, WhenVariableIsNullBuilder.class);
        builders.put(BuilderType.ConditionWhenExpressionIsTrue, WhenExpressionIsTrueBuilder.class);
        builders.put(BuilderType.AddContent, AddContentBuilder.class);
        builders.put(BuilderType.Publish, PublishBuilder.class);
        builders.put(BuilderType.Get, GetBuilder.class);
        builders.put(BuilderType.Post, PostBuilder.class);
        builders.put(BuilderType.Put, PutBuilder.class);
        builders.put(BuilderType.Delete, DeleteBuilder.class);
        builders.put(BuilderType.HttpStatusValidation, HttpStatusValidationBuilder.class);
        builders.put(BuilderType.BodyPublisher, BodyPublisherBuilder.class);
        builders.put(BuilderType.HttpResponseBodyContains, BodyContainsBuilder.class);
    }

    static boolean isBuilderNode(JsonNode node) {
        AtomicBoolean containsType = new AtomicBoolean(false);
        if (node.has("type")) {
            containsType.set(true);
        } else {
            node.iterator().forEachRemaining(entry -> {
                if (entry.has("type")) {
                    containsType.set(true);
                }
            });
        }
        return !node.isValueNode() && containsType.get();
    }

    static void depthFirstPreOrderFullTraversal(int depth,
                                                Set<Map.Entry<String, JsonNode>> visitedNodeIds,
                                                List<Map.Entry<String, JsonNode>> ancestors,
                                                Map.Entry<String, JsonNode> currentEntry,
                                                BiConsumer<List<Map.Entry<String, JsonNode>>, Map.Entry<String, JsonNode>> visit) {
        if (!visitedNodeIds.add(currentEntry)) {
            //LOG.error("Ignore: {} = {}", currentEntry.getKey(), currentEntry.getValue());
            return;
        }

        if (isBuilderNode(currentEntry.getValue())) {
            visit.accept(ancestors, currentEntry);
        }

        ancestors.add(currentEntry);
        try {
            // .fields iterer ikke over array
//            System.out.printf("%s = %s <= %s%n", currentEntry.getKey(), isBuilderNode(currentEntry.getValue()), currentEntry.getValue().fields().hasNext());
            if (currentEntry.getValue().isArray()) {
                Integer index = 0;
                for (Iterator<JsonNode> it = currentEntry.getValue().iterator(); it.hasNext(); ) {
                    depthFirstPreOrderFullTraversal(depth + 1, visitedNodeIds, ancestors, Map.entry(index.toString(), it.next()), visit);
                    index++;
                }
            } else {
                for (Iterator<Map.Entry<String, JsonNode>> it = currentEntry.getValue().fields(); it.hasNext(); ) {
                    depthFirstPreOrderFullTraversal(depth + 1, visitedNodeIds, ancestors, it.next(), visit);
                }
            }
        } finally {
            ancestors.remove(currentEntry);
        }
    }

    JsonNode deserializeSpecification() throws JsonProcessingException {
        String json = BuilderTest.SPECIFICATION_BUILDER.serialize();
        return mapper.readValue(json, JsonNode.class);
    }

    @Test
    public void traverse() throws JsonProcessingException {
        JsonNode rootNode = deserializeSpecification();
//        LOG.trace("\n{}", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode));
//        LOG.trace("size: {}", rootNode.size());

        depthFirstPreOrderFullTraversal(0, new LinkedHashSet<>(), new LinkedList<>(), Map.entry("ROOT", rootNode), (ancestors, current) -> {
            String indent = Arrays.stream(new String[ancestors.size()]).map(element -> "  ").collect(Collectors.joining());
            Map.Entry<String, JsonNode> last = ancestors.size() > 0 ? ancestors.get(ancestors.size() - 1) : null;
            System.out.printf("%s%s --> %s [%s]%n", indent, current.getKey(), current.getValue().isArray() ? "[" + last.getKey() + "]" : current.getValue(), current.getValue().isValueNode());
        });
    }

    // ============================================================================================================
    // Plan start
    // ============================================================================================================

    /*

    builder must have setters for all target

    the type of setters are:

    - field(value)
    - list(value)
    - map(key, object)
    - nested objects

    thoughts:

    - scan a class
    - find annotated fields and methods
    - make a map of bindings
    - a binder should be used for accessor operations

     */

    // ============================================================================================================
    // Plan end
    // ============================================================================================================


    @Test
    void targetBuilder() {
        BuilderReflection reflection = new BuilderReflection(builders.get(BuilderType.SpecificationContext));
        Collection<AbstractReflection> list = reflection.read();
        for (AbstractReflection target : list) {
            String name = target.getName();
            LOG.trace("name: {}, {}", name, target);
        }
    }

    static class BuilderReflection {
        private final Class<? extends AbstractBuilder> clazz;

        BuilderReflection(Class<? extends AbstractBuilder> clazz) {
            this.clazz = clazz;
        }

        Collection<AbstractReflection> read() {
            Map<String, AbstractReflection> targets = new LinkedHashMap<>();

            List<Class<? extends Annotation>> targetAnnotationClasses = List.of(JsonProperty.class, SetterMethod.class);

            for (Field field : clazz.getDeclaredFields()) {
                if (targetAnnotationClasses.stream().anyMatch(field::isAnnotationPresent)) {
                    targets.put(field.getName(), new FieldReflection(field));
                }
            }

            for (Method method : clazz.getDeclaredMethods()) {
                if (targetAnnotationClasses.stream().anyMatch(method::isAnnotationPresent)) {
                    String annoPropName = method.getAnnotation(JsonProperty.class).value(); // it's like hasAnno, getAno, getValue
                    targets.put(method.getName(), new MethodReflection(method));
                }
            }

            return targets.values();
        }
    }

    abstract static class AbstractReflection {

        abstract String getName();

        abstract boolean hasAnnotation(Class<? extends Annotation> annotationClass);
    }


    static class FieldReflection extends AbstractReflection {
        private final Field field;

        public FieldReflection(Field field) {
            this.field = field;
        }

        @Override
        String getName() {
            return field.getName();
        }

        @Override
        boolean hasAnnotation(Class<? extends Annotation> annotationClass) {
            return field.isAnnotationPresent(annotationClass);
        }
    }

    static class MethodReflection extends AbstractReflection {
        private final Method method;

        public MethodReflection(Method method) {
            this.method = method;
        }

        @Override
        String getName() {
            return method.getName();
        }

        @Override
        boolean hasAnnotation(Class<? extends Annotation> annotationClass) {
            return method.isAnnotationPresent(annotationClass);
        }
    }

}
