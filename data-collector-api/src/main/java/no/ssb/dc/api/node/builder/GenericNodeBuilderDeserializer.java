package no.ssb.dc.api.node.builder;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

// https://github.com/statisticsnorway/json-stat.java/blob/master/src/main/java/no/ssb/jsonstat/v2/deser/DimensionDeserializer.java
public class GenericNodeBuilderDeserializer extends StdDeserializer<GenericNodeBuilderDeserializer.Element.Builder> {

    protected GenericNodeBuilderDeserializer() {
        super(AbstractBuilder.class);
    }

    String indent(int depth) {
        return Arrays.stream(new String[depth]).map(element -> " ").collect(Collectors.joining());
    }

    StringBuilder builder(int depth, StringBuilder builder) {
        return builder.append(indent(depth)).append(String.format("(%s)", depth));
    }

    @Override
    public Element.Builder deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
        Element.Builder elementBuilder = new Element.Builder(null);

        StringBuilder builder = new StringBuilder();

        handleNode(0, parser, context, elementBuilder, builder);

        System.out.printf("builder:%n%s%n", builder.toString());

        return elementBuilder;
    }

    void handleNode(int depth, JsonParser parser, DeserializationContext context, Element.Builder elementBuilder, StringBuilder builder) throws IOException {

        JsonToken currentToken = parser.currentToken();

        if (currentToken != null) {
            builder(depth, builder).append(String.format("BEGIN %s %s ", currentToken.name(), currentToken.asString())).append(parser.currentName()).append(" ").append(parser.getValueAsString()).append("\n");
        }

        JsonToken fieldToken = parser.nextToken();
        if (fieldToken == JsonToken.START_OBJECT) {
            builder(depth, builder).append(String.format("--> START_OBJECT: %s %s ", fieldToken.name(), fieldToken.asString())).append(" ").append(parser.currentName()).append(" ").append(parser.getValueAsString()).append("\n");

        } else if (fieldToken == JsonToken.FIELD_NAME) {

            Property.Builder propertyBuilder = new Property.Builder();
            propertyBuilder.name(parser.currentName());
            propertyBuilder.value(parser.getValueAsString());

            builder(depth + 1, builder).append(String.format("%s %s ", fieldToken.name(), fieldToken.asString()))
                    .append(" => ").append(parser.currentName()).append(": ").append(parser.getValueAsString())
                    .append("\n");

                JsonToken nextValue;
                while ((nextValue = parser.nextValue()) == JsonToken.VALUE_STRING) {
                    builder(depth, builder).append(" fieldValue: ").append(nextValue.name()).append(" -> ").append(parser.currentName()).append(": ").append(parser.getValueAsString()).append("\n");
                }

                if (nextValue == JsonToken.START_ARRAY) {
                    builder(depth, builder).append("START array: ").append(nextValue.name()).append(" ").append(nextValue.asString()).append(" ").append(currentToken.asString()).append(" ").append(parser.currentName()).append(" ").append(parser.getValueAsString()).append(" ");
                    JsonToken nextToken = parser.nextToken();
                    builder.append(nextToken).append(" ").append(nextToken.asString()).append(" ").append(parser.getValueAsString()).append("\n");
                    handleNode(depth + 1, parser, context, elementBuilder, builder);
                }

                builder(depth, builder).append("END fieldValue: ").append(nextValue.name()).append(" -> ").append(parser.currentName()).append(": ").append(parser.getValueAsString()).append("\n");

                handleNode(depth + 1, parser, context, elementBuilder, builder);
        }

        if (fieldToken != null) {
            builder(depth, builder).append("end ").append(fieldToken.name()).append(" ").append(fieldToken.asString()).append("\n");
            handleNode(depth, parser, context, elementBuilder, builder);
        }
    }

    static class Element {

        static class Builder {
            private Element parent;

            public Builder(Element parent) {
                this.parent = parent;
            }

            Element build() {
                return new Element();
            }
        }
    }

    public static class Property {
        public final String name;
        public final String value;

        public Property(String name, String value) {
            this.name = name;
            this.value = value;
        }

        static class Builder {
            private String name;
            private String value;

            Builder name(String name) {
                this.name = name;
                return this;
            }

            Builder value(String value) {
                this.value = value;
                return this;
            }

            Property build() {
                return new Property(name, value);
            }
        }
    }
}


