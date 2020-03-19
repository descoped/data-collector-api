package no.ssb.dc.api.node.builder;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// https://github.com/statisticsnorway/json-stat.java/blob/master/src/main/java/no/ssb/jsonstat/v2/deser/DimensionDeserializer.java
public class GenericNodeBuilderDeserializer extends StdDeserializer<GenericNodeBuilderDeserializer.Node.NodeBuilder> {

    protected GenericNodeBuilderDeserializer() {
        super(AbstractBuilder.class);
    }

    static String indent(int depth) {
        return Arrays.stream(new String[depth]).map(element -> " ").collect(Collectors.joining());
    }

    StringBuilder builder(int depth, StringBuilder builder) {
        return builder.append(indent(depth)).append(String.format("(%s)", depth));
    }

    @Override
    public Node.NodeBuilder deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
        Node.NodeBuilder elementBuilder = new Node.NodeBuilder();

        StringBuilder builder = new StringBuilder();

        handleNode_(0, parser, context, elementBuilder, builder);

        System.out.printf("builder:%n%s%n", builder.toString());

        return elementBuilder;
    }

    void handleNode(int depth, JsonParser parser, DeserializationContext context, Node.NodeBuilder elementBuilder, StringBuilder builder) throws IOException {

        JsonToken currentToken = parser.currentToken();
        // check CurrentToken -> START_OBJECT
        Node.NodeBuilder currentElementBuilder;

        // new named property
        if (currentToken == JsonToken.START_OBJECT && parser.currentName() != null) {
            currentElementBuilder = new Node.NodeBuilder();
            currentElementBuilder.name(parser.currentName());
//            elementBuilder.addChild(currentElementBuilder);
            // already have a named start-object (root is always unnamed)
        } else {
            currentElementBuilder = elementBuilder;
        }

        // check NextToken -> START_OBJECT
        // check NextToken -> FIELD_NAME

        JsonToken fieldToken = parser.nextToken();
        if (fieldToken == JsonToken.START_OBJECT) {
            // do nothing

        } else if (fieldToken == JsonToken.FIELD_NAME) {
            JsonToken nextValue;
            while (FieldType.isValueToken((nextValue = parser.nextValue()))) {
                Field.FieldBuilder propertyBuilder = new Field.FieldBuilder();
                propertyBuilder.name(parser.currentName());
//                propertyBuilder.value(parser.getCurrentValue(), FieldType.of(nextValue));
//                currentElementBuilder.addProperty(propertyBuilder);
            }

            if (nextValue == JsonToken.START_ARRAY) {
                Array.ArrayBuilder arrayBuilder = new Array.ArrayBuilder();
//                elementBuilder.addChild(arrayBuilder);
            }

        } else if (fieldToken == JsonToken.END_OBJECT || fieldToken == JsonToken.END_ARRAY) {


        } else {
            if (fieldToken != null) {
                throw new IllegalStateException();
            }
        }

    }

    void handleNode_(int depth, JsonParser parser, DeserializationContext context, Node.NodeBuilder elementBuilder, StringBuilder builder) throws IOException {

        JsonToken currentToken = parser.currentToken();

        if (currentToken != null) {
            builder(depth, builder).append(String.format("BEGIN %s %s ", currentToken.name(), currentToken.asString())).append(parser.currentName()).append(" ").append(parser.getValueAsString()).append("\n");
        }

        JsonToken fieldToken = parser.nextToken();
        if (fieldToken == JsonToken.START_OBJECT) {
            builder(depth, builder).append(String.format("--> START_OBJECT: %s %s ", fieldToken.name(), fieldToken.asString())).append(" ").append(parser.currentName()).append(" ").append(parser.getValueAsString()).append("\n");

        } else if (fieldToken == JsonToken.FIELD_NAME) {

            builder(depth + 1, builder).append(String.format("%s %s ", fieldToken.name(), fieldToken.asString()))
                    .append(" => ").append(parser.currentName()).append(": ").append(parser.getValueAsString())
                    .append("\n");

            JsonToken nextValue;
            int n = 0;
            while ((nextValue = parser.nextValue()) == JsonToken.VALUE_STRING) {
                builder(depth, builder).append(" fieldValue: ").append(nextValue.name()).append(" -> ").append(parser.currentName()).append(": ").append(parser.getValueAsString()).append("\n");
                n++;
            }
            builder.append(" <--- ").append(n).append("\n");

            if (nextValue == JsonToken.START_ARRAY) {
                builder(depth, builder).append("START array: ").append(nextValue.name()).append(" ").append(nextValue.asString()).append(" ").append(currentToken.asString()).append(" ").append(parser.currentName()).append(" ").append(parser.getValueAsString()).append(" ");
                JsonToken nextToken = parser.nextToken();
                builder.append(nextToken).append(" ").append(nextToken.asString()).append(" ").append(parser.getValueAsString()).append("\n");
                handleNode_(depth + 1, parser, context, elementBuilder, builder);
            }

            builder(depth, builder).append("END fieldValue: ").append(nextValue.name()).append(" -> ").append(parser.currentName()).append(": ").append(parser.getValueAsString()).append("\n");

            handleNode_(depth + 1, parser, context, elementBuilder, builder);
        } else {
            //System.out.printf("-------------> %s%n", fieldToken);
        }

        if (fieldToken != null) {
            builder(depth, builder).append("end ").append(fieldToken.name()).append(" ").append(fieldToken.asString()).append("\n");
            handleNode_(depth, parser, context, elementBuilder, builder);
        }
    }

    /*
        Object, Array, Values
     */

    enum FieldType {
        STRING(JsonToken.VALUE_STRING),
        INTEGER(JsonToken.VALUE_NUMBER_INT),
        FLOAT(JsonToken.VALUE_NUMBER_FLOAT),
        BOOLEAN(JsonToken.VALUE_TRUE, JsonToken.VALUE_FALSE);

        private final List<JsonToken> jsonTokenList;

        FieldType(JsonToken... jsonToken) {
            jsonTokenList = List.of(jsonToken);
        }

        static FieldType of(JsonToken jsonToken) {
            for (FieldType type : values()) {
                if (type.jsonTokenList.contains(jsonToken)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("PropertyType doesn't support JsonToken: " + jsonToken);
        }

        static boolean isValueToken(JsonToken jsonToken) {
            for (FieldType type : values()) {
                if (type.jsonTokenList.contains(jsonToken)) {
                    return true;
                }
            }
            return false;
        }

        boolean isString() {
            return this == STRING;
        }

        boolean isInteger() {
            return this == INTEGER;
        }

        boolean isFloat() {
            return this == FLOAT;
        }

        boolean isBoolean() {
            return this == BOOLEAN;
        }
    }


    abstract static class AbstractBuilder<V> {
        abstract public V build();
    }

    abstract static class Named {
        abstract public String name();
    }

    abstract static class NamedBuilder<T, V> extends AbstractBuilder<V> {
        protected String name;

        T name(String name) {
            this.name = name;
            return (T) this;
        }

        abstract public Map<String, NamedBuilder> getChildren();

    }

    static class Array {



        static class ArrayBuilder extends AbstractBuilder<Array> {
            private final List<AbstractBuilder> nodeList = new ArrayList<>();

            public ArrayBuilder() {
            }

            ArrayBuilder node(AbstractBuilder node) {
                nodeList.add(node);
                return this;
            }

            @Override
            public Array build() {
                List<Object> nodes = nodeList.stream().map(item -> item.build()).collect(Collectors.toList());
                return new Array();
            }
        }
    }

    static class Node extends Named {
        public final String name;
        public final Map<String, Object> children;

        public Node(String name, Map<String, Object> children) {
            this.name = name;
            this.children = children;
        }

        @Override
        public String name() {
            return this.name;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "name='" + name + '\'' +
                    ", children=" + children +
                    '}';
        }

        static class NodeBuilder extends NamedBuilder<NodeBuilder, Node> {
            private final Map<String, AbstractBuilder<Node>> children = new LinkedHashMap<>();

            NodeBuilder addChild(NamedBuilder childBuilder) {
                children.put(childBuilder.name, childBuilder);
                return this;
            }

            NodeBuilder addChild(String name, AbstractBuilder childBuilder) {
                children.put(name, childBuilder);
                return this;
            }

            @Override
            public Map<String, NamedBuilder> getChildren() {
//                return children;
                return null;
            }

            @Override
            public Node build() {
                Map<String, Object> nodeList = children.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (k, v) -> ((AbstractBuilder<Node>) v).build(), LinkedHashMap::new));
                return new Node(name, nodeList);
            }
        }
    }

    public static class Field {
        public final String name;
        public final Value value;

        public Field(String name, Value value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String toString() {
            return "Field{" +
                    "name='" + name + '\'' +
                    ", value=" + value +
                    '}';
        }

        static class FieldBuilder extends NamedBuilder<FieldBuilder, Field> {
            private Value value;

            public FieldBuilder() {
            }

            public String name() {
                return this.name;
            }

            FieldBuilder value(Value value) {
                this.value = value;
                return this;
            }

            @Override
            public Field build() {
                return new Field(name, value);
            }

            @Override
            public Map<String, NamedBuilder> getChildren() {
                throw new UnsupportedOperationException();
            }
        }
    }

    static class Value {
        private final Object value;
        private final FieldType type;

        public Value(Object value, FieldType type) {
            this.value = value;
            this.type = type;
        }

        String asString() {
            return (String) value;
        }

        Integer asInteger() {
            return (Integer) value;
        }

        Float asFloat() {
            return (Float) value;
        }

        Boolean asBoolean() {
            return (Boolean) value;
        }

        @Override
        public String toString() {
            return "Value{" +
                    "value=" + value +
                    ", type=" + type +
                    '}';
        }
    }
}


