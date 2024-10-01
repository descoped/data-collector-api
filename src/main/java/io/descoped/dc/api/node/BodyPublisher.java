package io.descoped.dc.api.node;

import java.util.List;

public interface BodyPublisher extends OperationPublisher {

    String BODY_PUBLISHER_RESULT = "BODY_PUBLISHER_RESULT";

    FormEncoding getEncoding();

    BodyPublisherProducer getPlainText();

    BodyPublisherProducer getUrlEncodedData();

    List<BodyPart> getParts();

}
