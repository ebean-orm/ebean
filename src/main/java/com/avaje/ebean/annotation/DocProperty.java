package com.avaje.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specify the entity type maps to a document store (like ElasticSearch).
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface DocProperty {

  /**
   * Set this to true to indicate that this property should be un-analysed.
   */
  boolean code() default false;

  /**
   * Set this to true to get an additional un-analysed 'raw' field to use for sorting etc.
   */
  boolean sortable() default false;

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
