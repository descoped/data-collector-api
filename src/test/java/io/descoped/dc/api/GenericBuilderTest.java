package io.descoped.dc.api;

import io.descoped.dc.api.node.builder.AbstractBuilder;
import io.descoped.dc.api.node.builder.BuilderType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GenericBuilderTest {

    @Test
    public void spec() {
        Class<? extends AbstractBuilder> clazz = BuilderType.Specification.builderClass;
        System.out.printf("%s%n", clazz);
        List.of(clazz.getDeclaredFields()).forEach(field -> System.out.printf("%s%n", field.getName()));

        ReflectionTarget reflectionTarget = new ReflectionTarget(clazz);
    }

    @Test
    public void specContext() {
        Class<? extends AbstractBuilder> clazz = BuilderType.SpecificationContext.builderClass;
        System.out.printf("%s%n", clazz);
        List.of(clazz.getDeclaredFields()).forEach(field -> System.out.printf("%s%n", field.getName()));

        ReflectionTarget reflectionTarget = new ReflectionTarget(clazz);
        System.out.println();
        reflectionTarget.fieldTargets.forEach((k, v) -> System.out.printf("%s=%s%n", k, v));
        System.out.println();
        reflectionTarget.methodTargets.forEach((k, v) -> System.out.printf("%s=%s%n", k, v));
    }

    public static class ReflectionTarget {

        final Map<String, FieldTarget> fieldTargets = new LinkedHashMap<>();
        final Map<String, MethodTarget> methodTargets = new LinkedHashMap<>();

        public ReflectionTarget(Class<? extends AbstractBuilder> clazz) {
            // scanFields
            List.of(clazz.getDeclaredFields()).forEach(field -> fieldTargets.put(field.getName(), new FieldTarget(field)));

            // scanMethods
            List.of(clazz.getDeclaredMethods()).forEach(method -> methodTargets.put(method.getName(), new MethodTarget(method)));


        }
    }

    public static class FieldTarget {
        final String fieldName;
        final Field field;
        final String mappingName;

        public FieldTarget(Field field) {
            this.fieldName = field.getName();
            this.field = field;
            this.mappingName = field.isAnnotationPresent(ConfigSetter.class) ? field.getAnnotation(ConfigSetter.class).toString() : field.getName();
        }

        @Override
        public String toString() {
            return "FieldTarget{" +
                    "fieldName='" + fieldName + '\'' +
                    ", field=" + field +
                    ", mappingName='" + mappingName + '\'' +
                    '}';
        }
    }

    public static class MethodTarget {
        final String methodName;
        final Method method;
        final String mappingName;

        public MethodTarget(Method method) {
            this.methodName = method.getName();
            this.method = method;
            this.mappingName = method.isAnnotationPresent(ConfigSetter.class) ? method.getAnnotation(ConfigSetter.class).toString() : method.getName();
            ;
        }

        @Override
        public String toString() {
            return "MethodTarget{" +
                    "methodName='" + methodName + '\'' +
                    ", method=" + method +
                    ", mappingName='" + mappingName + '\'' +
                    '}';
        }
    }
}
