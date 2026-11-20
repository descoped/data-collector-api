package io.descoped.dc.api;

import io.descoped.dc.api.node.builder.SpecificationBuilder;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Every specification shipped as an example, in {@code examples/specs} and the server's {@code page-test.json}, reads
 * and builds as the server does with a task ({@code PUT /tasks}), so the examples keep up with the DSL.
 */
public class ExampleSpecificationsTest {

    private static final Path REPOSITORY = repository();

    /** The repository root: the nearest directory above the working directory that has {@code examples/specs}. */
    private static Path repository() {
        for (Path dir = Path.of("").toAbsolutePath(); dir != null; dir = dir.getParent()) {
            if (Files.isDirectory(dir.resolve("examples").resolve("specs"))) {
                return dir;
            }
        }
        throw new IllegalStateException("examples/specs not found above " + Path.of("").toAbsolutePath());
    }

    @TestFactory
    Stream<DynamicTest> exampleSpecificationsReadAndBuild() throws IOException {
        List<Path> specifications;
        try (Stream<Path> files = Files.list(REPOSITORY.resolve("examples").resolve("specs"))) {
            specifications = files.filter(path -> path.toString().endsWith(".json")).sorted().toList();
        }
        assertFalse(specifications.isEmpty(), "no specifications in examples/specs");
        Path pageTest = REPOSITORY.resolve("data-collector-server/src/main/resources/worker.config/page-test.json");
        return Stream.concat(specifications.stream(), Stream.of(pageTest))
                .map(path -> DynamicTest.dynamicTest(REPOSITORY.relativize(path).toString(), () -> {
                    SpecificationBuilder builder = Specification.deserialize(Files.readString(path));
                    assertNotNull(builder.end());
                }));
    }
}
