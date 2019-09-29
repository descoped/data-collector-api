package no.ssb.dc.api;

import no.ssb.dc.api.context.ExecutionContext;
import org.testng.annotations.Test;

import static no.ssb.dc.api.Builders.execute;
import static no.ssb.dc.api.Builders.get;
import static no.ssb.dc.api.Builders.nextPage;
import static no.ssb.dc.api.Builders.paginate;
import static no.ssb.dc.api.Builders.parallel;
import static no.ssb.dc.api.Builders.process;
import static no.ssb.dc.api.Builders.publish;
import static no.ssb.dc.api.Builders.regex;
import static no.ssb.dc.api.Builders.sequence;
import static no.ssb.dc.api.Builders.whenVariableIsNull;
import static no.ssb.dc.api.Builders.xpath;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class BuilderTest {

    static final Flow.FlowBuilder flowBuilder = Flow.start("name of flow", "getstartposition")
            .node(get("getstartposition")
                    .url("http://com.company/getstartposition")
                    .step(process(A.class).output("next-position"))
                    .step(process(B.class).output("another-position").output("yet-another"))
                    .step(execute("page-loop").requiredInput("next-position"))
            )
            .node(paginate("page-loop")
                    .variable("from-position", "${next-position}")
                    .step(execute("page"))
                    .prefetchThreshold(0.5)
                    .until(whenVariableIsNull("next-position"))
            )
            .node(get("page")
                    .url("http://com.company/endpoint?seq=${from-position}&pageSize=10")
                    // build expected position list
                    .step(sequence(xpath("/feed/entry"))
                            .expected(xpath("/entry/content/ns2:lagretHendelse/ns2:sekvensnummer"))
                    )
                    // propagate next position to paginate
                    .step(nextPage().output("next-position", regex(xpath("/feed/link[@rel=\"next\"]/@href"), "(?<=[?&]seq=)[^&]*")))
                    // parallel should take the sequence as input
                    .step(parallel(xpath("/feed/entry"))
                            .variable("position", xpath("/entry/content/ns2:lagretHendelse/ns2:sekvensnummer"))
                            .step(execute("person-doc")
                                    .inputVariable("person-id", xpath("/entry/content/ns2:lagretHendelse/ns2:hendelse/ns2:persondokument"))
                                    .requiredInput("person-id-blash")
                                    .inputVariable("person-id2", xpath("/entry/content/ns2:lagretHendelse/ns2:hendelse/ns2:persondokument"))
                            )
                            .step(execute("event-doc")
                                    .inputVariable("event-id", xpath("/entry/content/ns2:lagretHendelse/ns2:hendelse/ns2:hendelsesdokument"))
                            )
                            // publish completed position. Sequencing should occur in core
                            .step(publish("${position}"))
                    )
                    .step(process(ItemList.class).output("next-position")) // alternative to sequence() and parallel()
            )
            .node(get("person-doc")
                    .url("http://com.company/endpoint/person/${person-id}}")
                    .step(process(Processor.class).output("person-id"))
            )
            .node(get("event-doc")
                    .url("http://com.company/endpoint/event/${event-id}}")
                    .step(process(Processor.class).output("event-id"))
            );

    @Test
    public void printExecutionPlan() {
        System.out.printf("Execution-plan:%n%n%s%n", flowBuilder.end().startNode().toPrintableExecutionPlan());
    }

    @Test
    public void thatFlowBuilderIsSerializedThenDeserialized() {
        Flow.FlowBuilder actual = flowBuilder;
        String serialized = actual.serialize();
        assertNotNull(serialized);
        System.out.printf("%s%n", serialized);

        Flow.FlowBuilder deserialized = Flow.deserialize(serialized, Flow.FlowBuilder.class);
        assertNotNull(deserialized);
        System.out.printf("%s%n", serialized);

        assertEquals(actual, deserialized);
    }

    @Test
    public void thatGettingStartedBuilderIsSerializedThenDeserialized() {
        Flow.NodeBuilder actual = flowBuilder.get("getstartposition");
        String serialized = actual.serialize();
        assertNotNull(serialized);

        Flow.GetBuilder deserialized = Flow.deserialize(serialized, Flow.GetBuilder.class);
        assertNotNull(deserialized);

        assertEquals(actual, deserialized);
    }

    @Test
    public void thatPageLoopBuilderIsSerializedThenDeserialized() {
        Flow.NodeBuilder actual = flowBuilder.get("page-loop");
        String serialized = actual.serialize();
        assertNotNull(serialized);

        Flow.PaginateBuilder deserialized = Flow.deserialize(serialized, Flow.PaginateBuilder.class);
        assertNotNull(deserialized);

        assertEquals(actual, deserialized);
    }

    @Test
    public void thatPageGetBuilderIsSerializedThenDeserialized() {
        Flow.NodeBuilder actual = flowBuilder.get("page");
        String serialized = actual.serialize();
        assertNotNull(serialized);

        Flow.GetBuilder deserialized = Flow.deserialize(serialized, Flow.GetBuilder.class);
        assertNotNull(deserialized);

        assertEquals(actual, deserialized);
    }

    @Test
    public void thatPersonDocGetBuilderIsSerializedThenDeserialized() {
        Flow.NodeBuilder actual = flowBuilder.get("person-doc");
        String serialized = actual.serialize();
        assertNotNull(serialized);

        Flow.GetBuilder deserialized = Flow.deserialize(serialized, Flow.GetBuilder.class);
        assertNotNull(deserialized);

        assertEquals(actual, deserialized);
    }

    @Test
    public void thatEventDocGetBuilderIsSerializedThenDeserialized() {
        Flow.NodeBuilder actual = flowBuilder.get("event-doc");
        String serialized = actual.serialize();
        assertNotNull(serialized);

        Flow.GetBuilder deserialized = Flow.deserialize(serialized, Flow.GetBuilder.class);
        assertNotNull(deserialized);

        assertEquals(actual, deserialized);
    }

    static class A implements Processor {
        @Override
        public ExecutionContext process(ExecutionContext input) {
            return null;
        }
    }

    static class B implements Processor {
        @Override
        public ExecutionContext process(ExecutionContext input) {
            return null;
        }
    }

}
