package io.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for declaring multiple indices at class or field level.
 *
 * @author Roland Praml, FOCONIS AG
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Indices {

  /**
   * Array with {@link Index} definitions.
   */
  Index[] value() default {};
}
