package com.avaje.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to indicate a property on an entity bean used to control 'soft delete'
 * (also known as 'logical delete').
 * <p>
 * The property should be of type boolean, int or short.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SoftDelete {

  /**
   * Specify the bind value that matches 'deleted' state.
   * <p>
   *   If not specified then for boolean this is <code>true</code> and
   *   for int and short this value is <code>1</code>.
   * </p>
   */
  String value() default "";
}
