package io.descoped.dc.application.controller;

import io.descoped.dc.api.health.HealthResource;
import io.descoped.dc.application.health.HealthModuleInfoResource;
import io.descoped.dc.application.health.HealthResourceFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HealthModuleInfoResourceControllerTest {

    private static final Logger LOG = LoggerFactory.getLogger(HealthModuleInfoResourceControllerTest.class);

    @Test
    public void thatModuleListHasTheDescopedModulesOfTheBootLayer() {
        HealthModuleInfoResource healthModuleInfoResource = new HealthModuleInfoResource();
        @SuppressWarnings("unchecked")
        List<HealthModuleInfoResource.ModuleInfo> modules = (List<HealthModuleInfoResource.ModuleInfo>) healthModuleInfoResource.resource();
        Set<String> names = modules.stream().map(info -> info.moduleName).collect(Collectors.toSet());
        // server-base's own module and the modules it requires are always resolved in its tests
        assertTrue(names.containsAll(Set.of(
                        "io.descoped.dc.application",
                        "io.descoped.dc.api",
                        "io.descoped.service.provider.api",
                        "io.descoped.dynamic.config")),
                () -> "missing modules in " + names);
        assertTrue(names.stream().allMatch(name -> name.startsWith("io.descoped")), () -> "not an io.descoped module in " + names);
        System.out.printf("%s -> %s%n", healthModuleInfoResource.name(), healthModuleInfoResource.resource());
    }

    @Test
    public void thatHealthResourcesAreDiscovered() {
        List<HealthResource> healthResources = HealthResourceFactory.create().getHealthResources();
        healthResources.forEach(healthResource -> System.out.printf("%s%n", healthResource));
    }

    @Test
    public void testThreadGroups() {
        Set<Thread> threads = Thread.getAllStackTraces().keySet();

        SortedMap<String, List<Thread>> threadGroups = new TreeMap<>(Comparator.comparing(String::toString));
        for (Thread thread : threads) {
            if (thread.getThreadGroup() == null) {
                LOG.warn("ThreadGroup is NULL for thread: {}", thread);
                continue;
            }
            String threadGroupName = thread.getThreadGroup().getName();
            List<Thread> threadList = threadGroups.computeIfAbsent(threadGroupName, list -> new ArrayList<>());
            List<StackTraceElement> stackTraceList = List.of(thread.getStackTrace());
            for (StackTraceElement stackTraceElement : stackTraceList) {
                System.out.printf("st: %s%n", stackTraceElement);
            }

            threadList.add(thread);
            threadGroups.put(threadGroupName, threadList);
        }

        for (Map.Entry<String, List<Thread>> threadEntry : threadGroups.entrySet()) {
            System.out.printf("%s -> %s%n", threadEntry.getKey(), threadEntry.getValue().stream().map(Thread::getName).collect(Collectors.joining(", ")));
        }
    }
}
