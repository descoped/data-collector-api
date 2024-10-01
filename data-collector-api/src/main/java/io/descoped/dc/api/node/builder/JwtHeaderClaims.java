package io.descoped.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;


// TODO make as builder

public class JwtHeaderClaims {

    @JsonProperty
    Map<String, String> headerClaims = new LinkedHashMap<>();

    public JwtHeaderClaims alg(String alg) {
        headerClaims.put("alg", alg);
        return this;
    }

    public JwtHeaderClaims x509CertChain(String sslBundleName) {
        headerClaims.put("sslBundleName", sslBundleName);
        return this;
    }

    public String alg() {
        return headerClaims.get("alg");
    }

    public String sslBundleName() {
        return headerClaims.get("sslBundleName");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JwtHeaderClaims that = (JwtHeaderClaims) o;
        return Objects.equals(headerClaims, that.headerClaims);
    }

    @Override
    public int hashCode() {
        return Objects.hash(headerClaims);
    }

    @Override
    public String toString() {
        return "JwtHeaderClaims{" +
                "headerClaims=" + headerClaims +
                '}';
    }
}
