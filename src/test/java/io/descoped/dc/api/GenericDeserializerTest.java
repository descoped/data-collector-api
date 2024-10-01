package io.descoped.dc.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.descoped.dc.api.node.NodeWithId;
import io.descoped.dc.api.node.builder.SpecificationBuilder;
import io.descoped.dc.api.util.JsonParser;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GenericDeserializerTest {

    private static final Logger LOG = LoggerFactory.getLogger(GenericDeserializerTest.class);

    //    @BeforeAll
    static void beforeAll() {
        try (ScanResult scanResult = new ClassGraph().enableAllInfo().whitelistPackages("io.descoped.dc").scan()) {
            ClassInfoList cl = scanResult.getClassesImplementing(NodeWithId.class.getName());
            cl.getNames().forEach(n -> LOG.debug("--> {}", n));
        }
    }

    @Test
    public void parseSpecAndCreateBuilders() {
        SpecificationBuilder actual = BuilderTest.SPECIFICATION_BUILDER;
        String serialized = actual.serialize();
        assertNotNull(serialized);

        ObjectNode rootNode = JsonParser.createJsonParser().fromJson(serialized, ObjectNode.class);

        traverse(0, new LinkedHashSet<>(), Map.entry("root", rootNode), this::handleEntry);

        LOG.trace(serialized);
    }

    private void handleEntry(Set<Map.Entry<String, JsonNode>> ancestors, Map.Entry<String, JsonNode> currentEntry) {
        String indent = Arrays.stream(new String[ancestors.size()]).map(element -> " ").collect(Collectors.joining());
        String currentName = currentEntry.getKey();
        JsonNode currentNode = currentEntry.getValue();
        String id = currentNode.has("id") ? currentNode.get("id").asText() : null;
        String type = currentNode.has("type") ? currentNode.get("type").asText() : null;

//        if (type != null) {
        LOG.trace("{}{}: {} ({}) {} @ {}", indent, currentName, currentNode.getNodeType(), type, id, ""); // JsonParser.createJsonParser().toJSON(currentEntry.getValue())
//        }

        if (currentEntry.getValue().isArray()) {
            for (int i = 0; i < currentEntry.getValue().size(); i++) {
                Map.Entry<String, JsonNode> childEntry = Map.entry(String.valueOf(i), currentEntry.getValue().get(i));
                if (isLiteralType(childEntry.getValue())) {
                    continue;
                }
                ancestors.add(childEntry);
                traverse(ancestors.size() + 1, ancestors, childEntry, this::handleEntry);
                ancestors.remove(childEntry);
            }
        }
    }

    /*
        traverse root
            isObject: print
            isArray: loop and traverse
     */

    void traverse(int depth,
                  Set<Map.Entry<String, JsonNode>> ancestors,
                  Map.Entry<String, JsonNode> currentEntry,
                  BiConsumer<Set<Map.Entry<String, JsonNode>>, Map.Entry<String, JsonNode>> visit) {

        visit.accept(ancestors, currentEntry);

        ancestors.add(currentEntry);

        for (Iterator<Map.Entry<String, JsonNode>> it = currentEntry.getValue().fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> entry = it.next();

            if (isLiteralType(entry.getValue())) {
                continue;
            }

            traverse(depth + 1, ancestors, Map.entry(entry.getKey(), entry.getValue()), visit);
        }

        ancestors.remove(currentEntry);
    }

    private boolean isLiteralType(JsonNode node) {
        return !(node.isObject() || node.isArray());
//        return false;
    }
}
