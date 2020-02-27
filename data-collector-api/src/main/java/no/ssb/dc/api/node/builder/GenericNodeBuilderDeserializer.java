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
public class GenericNodeBuilderDeserializer extends StdDeserializer<AbstractBuilder> {

    protected GenericNodeBuilderDeserializer() {
        super(AbstractBuilder.class);
    }

    @Override
    public AbstractBuilder deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
        StringBuilder builder = new StringBuilder();

        handleNode(0, parser, context, builder);

        System.out.printf("builder:%n%s%n", builder.toString());

        return null;
    }

    String indent(int depth) {
        return Arrays.stream(new String[depth]).map(element -> " ").collect(Collectors.joining());
    }

    StringBuilder builder(int depth, StringBuilder builder) {
        return builder.append(indent(depth)).append(String.format("(%s)", depth));
    }

    void handleNode(int depth, JsonParser parser, DeserializationContext context, StringBuilder builder) throws IOException {

        builder(depth, builder).append(String.format("%s %s ", parser.currentToken().name(), parser.currentToken().asString())).append("\n");
        JsonToken token;
        if ((token = parser.nextToken()) == JsonToken.FIELD_NAME) {
            Property.Builder propertyBuilder = new Property.Builder();
            propertyBuilder.name(parser.currentName());
            propertyBuilder.value(parser.getValueAsString());

            builder(depth + 1, builder).append(String.format("%s %s ", token.name(), token.asString()))
                    .append(" => ").append(parser.currentName()).append(": ").append(parser.getValueAsString())
                    .append("\n");

//            if (token == JsonToken.FIELD_NAME) {
                JsonToken nextValue;
                while ((nextValue = parser.nextValue()) != null) {
                    builder(depth, builder).append("fieldValue: ").append(parser.getValueAsString()).append("\n");
                }
//            }
        }
    }

    static class Element {
        static class Builder {
            Element build() {
                return new Element();
            }
        }
    }

    static class Property {
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


