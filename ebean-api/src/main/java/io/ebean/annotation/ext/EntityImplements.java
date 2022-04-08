package io.ebean.annotation.ext;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation to define a list of interface for which this entity is target for.
 * @author Roland Praml, FOCONIS AG
 */
@Documented
@Target({ FIELD, TYPE })
@Retention(RUNTIME)
public @interface EntityImplements {

  Class<?>[] value();

}
