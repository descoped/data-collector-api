package no.ssb.dc.api.node.impl;

import no.ssb.dc.api.node.WhenVariableIsNull;

import java.util.Objects;

public class WhenVariableIsNullNode extends ConditionNode implements WhenVariableIsNull {


    final String identifier;

    public WhenVariableIsNullNode(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String identifier() {
        return identifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WhenVariableIsNullNode that = (WhenVariableIsNullNode) o;
        return Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }

    @Override
    public String toString() {
        return "WhenVariableIsNullNode{" +
                "identifier='" + identifier + '\'' +
                '}';
    }
}
