package com.avaje.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
    Copied from gson!!!
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Expose {

  /**
   * If {@code true}, the field marked with this annotation is written out in the JSON while
   * serializing. If {@code false}, the field marked with this annotation is skipped from the
   * serialized output. Defaults to {@code true}.
   */
  boolean serialize() default true;

  /**
   * If {@code true}, the field marked with this annotation is deserialized from the JSON.
   * If {@code false}, the field marked with this annotation is skipped during deserialization.
   * Defaults to {@code true}.
   */
  boolean deserialize() default true;
}