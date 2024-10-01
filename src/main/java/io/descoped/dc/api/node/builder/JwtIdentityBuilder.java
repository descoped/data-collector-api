package io.descoped.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.descoped.dc.api.node.Base;
import io.descoped.dc.api.node.JwtIdentity;

import java.util.Objects;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class JwtIdentityBuilder extends IdentityBuilder {

    @JsonUnwrapped
    JwtHeaderClaims headerClaims;
    @JsonUnwrapped
    JwtClaims claims;

    public JwtIdentityBuilder(String id, JwtHeaderClaims headerClaims, JwtClaims claims) {
        super(BuilderType.JwtIdentity, id);
        this.headerClaims = headerClaims;
        this.claims = claims;
    }

    @Override
    public <R extends Base> R build(BuildContext buildContext) {
        return (R) new JwtIdentityNode(id, headerClaims, claims);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        JwtIdentityBuilder that = (JwtIdentityBuilder) o;
        return Objects.equals(headerClaims, that.headerClaims) &&
                Objects.equals(claims, that.claims);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), headerClaims, claims);
    }

    @Override
    public String toString() {
        return "JwtIdentityBuilder{" +
                "headerClaims=" + headerClaims +
                ", claims=" + claims +
                '}';
    }

    static class JwtIdentityNode extends LeafNode implements JwtIdentity {

        private final String id;
        private final JwtHeaderClaims headerClaims;
        private final JwtClaims claims;

        public JwtIdentityNode(String id, JwtHeaderClaims headerClaims, JwtClaims claims) {
            this.id = id;
            this.headerClaims = headerClaims;
            this.claims = claims;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public JwtHeaderClaims headerClaims() {
            return headerClaims;
        }

        @Override
        public JwtClaims claims() {
            return claims;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            JwtIdentityNode that = (JwtIdentityNode) o;
            return Objects.equals(id, that.id) &&
                    Objects.equals(headerClaims, that.headerClaims) &&
                    Objects.equals(claims, that.claims);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, headerClaims, claims);
        }

        @Override
        public String toString() {
            return "JwtIdentityNode{" +
                    "id='" + id + '\'' +
                    ", headerClaims=" + headerClaims +
                    ", claims=" + claims +
                    '}';
        }
    }
}
