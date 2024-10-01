package io.descoped.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class JwtClaims {

    @JsonProperty("claims")
    Map<String, String> claims = new LinkedHashMap<>();

    public JwtClaims issuer(String issuer) {
        claims.put("issuer", issuer);
        return this;
    }

    public JwtClaims audience(String audience) {
        claims.put("audience", audience);
        return this;
    }

    public JwtClaims timeToLiveInSeconds(String timeToLiveInSeconds) {
        claims.put("timeToLiveInSeconds", timeToLiveInSeconds);
        return this;
    }

    public JwtClaims claim(String key, String value) {
        this.claims.put(key, value);
        return this;
    }

    public String issuer() {
        return claims.get("issuer");
    }

    public String audience() {
        return claims.get("audience");
    }

    public String timeToLiveInSeconds() {
        return claims.get("timeToLiveInSeconds");
    }

    public Map<String, String> getClaims() {
        return claims;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JwtClaims claims1 = (JwtClaims) o;
        return Objects.equals(claims, claims1.claims);
    }

    @Override
    public int hashCode() {
        return Objects.hash(claims);
    }

    @Override
    public String toString() {
        return "JwtClaims{" +
                "claims=" + claims +
                '}';
    }
}
