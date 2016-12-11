package io.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to indicate that a particular string property should be treated as a 'code' and not analysed for text searching.
 * <p>
 * By default all Id properties and all Enum properties are treated as 'code' and not analysed.
 * </p>
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DocCode {

  /**
   * Set to true to have the property additionally stored separately from _source.
   */
  boolean store() default false;

  /**
   * Set a boost value specific to this property.
   */
  float boost() default 1;

  /**
   * Set a value to use instead of null.
   */
  String nullValue() default "";
}
