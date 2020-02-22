package no.ssb.dc.api.node.builder;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

// https://github.com/statisticsnorway/json-stat.java/blob/master/src/main/java/no/ssb/jsonstat/v2/deser/DimensionDeserializer.java
public class GenericNodeBuilderDeserializer extends StdDeserializer<AbstractBuilder> {

    protected GenericNodeBuilderDeserializer() {
        super(AbstractBuilder.class);
    }

    @Override
    public AbstractBuilder deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {

        // Get the name first.
        String name = parseName(p, ctxt);
        System.out.printf("name: %s%n", name);

        if (p.getCurrentToken() == JsonToken.START_OBJECT) {
            JsonToken token = p.nextToken();
            System.out.printf("token: %s%n", token.name());
        }

        while (p.nextValue() != JsonToken.END_OBJECT) {
            System.out.printf("current name: %s%n", p.getCurrentName());
        }


        return null;
    }

    private String parseName(JsonParser p, DeserializationContext ctxt) throws IOException {
        String name = p.getCurrentName();
        return name;
    }

}
