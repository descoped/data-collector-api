package no.ssb.dc.api;

import no.ssb.dc.api.node.BaseNode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Handler {
    Class<? extends BaseNode> forClass();
}
