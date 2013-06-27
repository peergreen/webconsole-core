package com.peergreen.webconsole.core.handler.extensions;

import com.peergreen.webconsole.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Mohammed Boukada
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Qualifier("test")
public @interface TestQualifier {
    String attr1();
    String attr2() default "";
    String attr3();
}
