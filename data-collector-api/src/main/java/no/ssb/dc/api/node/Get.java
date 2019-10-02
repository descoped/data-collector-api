package no.ssb.dc.api.node;

import no.ssb.dc.api.DefaultStringPositionProducer;
import no.ssb.dc.api.PositionProducer;
import no.ssb.dc.api.http.Headers;
import no.ssb.dc.api.services.InstanceCache;
import no.ssb.dc.api.services.ObjectCreator;
import no.ssb.dc.api.services.Services;

import java.util.List;

public interface Get extends Operation {

    String url();

    Headers headers();

    List<Validator> responseValidators();

    List<? extends Node> steps();

    Class<? extends PositionProducer> positionProducerClass();

    List<String> returnVariables();

    default PositionProducer<?> createOrGetPositionProducer() {
        boolean isDefaultProducer = positionProducerClass() == null;
        Class<? extends PositionProducer> producerClass = isDefaultProducer ? DefaultStringPositionProducer.class : positionProducerClass();

        if (InstanceCache.instance().has(producerClass)) {
            return InstanceCache.instance().get(producerClass);

        } else {
            if (isDefaultProducer) {
                return InstanceCache.instance().add(producerClass, new DefaultStringPositionProducer());
            } else {
                return InstanceCache.instance().add(producerClass, ObjectCreator.newInstance(positionProducerClass(), Services.create()));
            }
        }
    }
}
