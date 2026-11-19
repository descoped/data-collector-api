import java.io.IOException;
import java.lang.module.FindException;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Splits the server's jars into a module path and a class path, the way Maven Surefire runs the tests: the module path
 * holds the root module and every module it requires (static requires too, when the jar is there), plus the providers
 * of the services these modules use; every other jar goes on the class path. The launcher adds them all with
 * {@code --add-modules ALL-MODULE-PATH}.
 *
 * <p>Usage: {@code java SplitModulePath.java <lib dir> <class path dir> <root module>}. Jars that leave the module
 * path are moved to the class path directory.
 */
public class SplitModulePath {

    public static void main(String[] args) throws IOException {
        Path lib = Path.of(args[0]);
        Path classpath = Path.of(args[1]);
        String root = args[2];

        Map<String, Path> jarByModule = new HashMap<>();
        Map<String, ModuleDescriptor> descriptors = new HashMap<>();
        Set<Path> notModules = new HashSet<>();
        List<Path> jars;
        try (Stream<Path> files = Files.list(lib)) {
            jars = files.filter(p -> p.toString().endsWith(".jar")).sorted().collect(Collectors.toList());
        }
        for (Path jar : jars) {
            try {
                Optional<ModuleReference> reference = ModuleFinder.of(jar).findAll().stream().findFirst();
                if (reference.isPresent()) {
                    ModuleDescriptor descriptor = reference.get().descriptor();
                    jarByModule.put(descriptor.name(), jar);
                    descriptors.put(descriptor.name(), descriptor);
                } else {
                    notModules.add(jar);
                }
            } catch (FindException e) {
                // not a valid module (e.g. a jar name that is no Java identifier): class path
                notModules.add(jar);
            }
        }

        Set<String> resolved = new HashSet<>();
        Deque<String> pending = new ArrayDeque<>(List.of(root));
        while (!pending.isEmpty()) {
            String name = pending.pop();
            ModuleDescriptor descriptor = descriptors.get(name);
            if (descriptor == null || !resolved.add(name)) {
                continue;
            }
            // static requires too, when the jar is there: a named module cannot read the class path
            for (ModuleDescriptor.Requires requires : descriptor.requires()) {
                pending.push(requires.name());
            }
            for (String service : descriptor.uses()) {
                descriptors.values().stream()
                        .filter(d -> d.provides().stream().anyMatch(p -> p.service().equals(service)))
                        .forEach(d -> pending.push(d.name()));
            }
        }
        if (!resolved.contains(root)) {
            throw new IllegalStateException("Root module not found in " + lib + ": " + root);
        }

        Files.createDirectories(classpath);
        int moved = 0;
        for (Path jar : jars) {
            boolean onModulePath = jarByModule.entrySet().stream()
                    .anyMatch(e -> e.getValue().equals(jar) && resolved.contains(e.getKey()));
            if (!onModulePath || notModules.contains(jar)) {
                Files.move(jar, classpath.resolve(jar.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                moved++;
            }
        }
        System.out.printf("Module path: %d jars, class path: %d jars%n", jars.size() - moved, moved);
    }
}
