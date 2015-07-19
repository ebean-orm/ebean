package com.avaje.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Similar to Jackson JsonIgnore but provides the option to just ignore serialize or deserialize.
 * <p>
 *   This provides the same features as Expose but from the opposite perspective which is probably
 *   more common and more familiar to Jackson users.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JsonIgnore {

  /**
   * If {@code true}, the field marked with this annotation is written out in the JSON while
   * serializing. If {@code false}, the field marked with this annotation is skipped from the
   * serialized output. Defaults to {@code false}.
   */
  boolean serialize() default false;

  /**
   * If {@code true}, the field marked with this annotation is deserialized from the JSON.
   * If {@code false}, the field marked with this annotation is skipped during deserialization.
   * Defaults to {@code false}.
   */
  boolean deserialize() default false;
}