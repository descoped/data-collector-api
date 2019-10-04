package no.ssb.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import no.ssb.dc.api.node.Base;
import no.ssb.dc.api.node.Security;

import java.util.Objects;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class SecurityBuilder extends ConfigurationBuilder {

    @JsonProperty String bundleName;

    public SecurityBuilder() {
        super(BuilderType.Security);
    }

    public SecurityBuilder sslBundlename(String bundleName) {
        this.bundleName = bundleName;
        return this;
    }

    @Override
    public <R extends Base> R build(BuildContext buildContext) {
        return (R) new SecurityNode(bundleName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SecurityBuilder that = (SecurityBuilder) o;
        return bundleName.equals(that.bundleName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), bundleName);
    }

    @Override
    public String toString() {
        return "SecurityBuilder{" +
                "bundleName='" + bundleName + '\'' +
                '}';
    }

    static class SecurityNode extends LeafNode implements Security {

        private String sslBundleName;

        public SecurityNode(String sslBundleName) {
            this.sslBundleName = sslBundleName;
        }

        @Override
        public String sslBundleName() {
            return sslBundleName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SecurityNode that = (SecurityNode) o;
            return Objects.equals(sslBundleName, that.sslBundleName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sslBundleName);
        }

        @Override
        public String toString() {
            return "SecurityNode{" +
                    "sslBundleName='" + sslBundleName + '\'' +
                    '}';
        }
    }
}
