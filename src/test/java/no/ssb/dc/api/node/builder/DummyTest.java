package no.ssb.dc.api.node.builder;

import org.junit.jupiter.api.Test;

/**
 *  Node
 *      Field
 *          Value
 *      Node
 *          Field
 *              Value
 *      Array
 *          Field
 *              Value
 *  Array
 *      Value
 *      Node
 *          Field
 *              Value
 */
public class DummyTest {

    @Test
    void name() {

    }

    static class ArrayObjectBuilder {
    }

    static class NodeObjectBuilder {
        String name;
        FieldObjectBuilder field;

        NodeObjectBuilder name(String name) {
            this.name = name;
            return this;
        }



    }

    static class FieldObjectBuilder {
    }

    static class ValueObjectBuilder {

    }
}

