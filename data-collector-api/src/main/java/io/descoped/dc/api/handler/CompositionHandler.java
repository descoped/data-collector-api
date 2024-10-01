package io.descoped.dc.api.handler;

import io.descoped.dc.api.node.Base;
import io.descoped.dc.api.node.Query;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface CompositionHandler {
    Class<? extends Base> forClass();

    Class<? extends Query> selectorClass();

}
