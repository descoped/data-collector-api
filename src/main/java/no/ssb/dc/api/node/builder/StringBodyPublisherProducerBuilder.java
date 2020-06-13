package no.ssb.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import no.ssb.dc.api.node.Base;
import no.ssb.dc.api.node.StringBodyPublisherProducer;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class StringBodyPublisherProducerBuilder extends BodyPublisherProducerBuilder {

    @JsonProperty String data;

    public StringBodyPublisherProducerBuilder(String data) {
        super(BuilderType.BodyPublisherProducer);
        this.data = data;
    }

    @Override
    public <R extends Base> R build(BuildContext buildContext) {
        return (R) new StringBodyPublisherProducerNode(data);
    }

    public class StringBodyPublisherProducerNode extends LeafNode implements StringBodyPublisherProducer {

        private final String text;

        public StringBodyPublisherProducerNode(String text) {
            this.text = text;
        }

        @Override
        public String text() {
            return text;
        }
    }
}
