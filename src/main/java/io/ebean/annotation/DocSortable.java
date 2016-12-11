package io.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to indicate that a particular string property should support sorting. What this typically means is that
 * for ElasticSearch an additional 'raw' field is added that stores the un-analysed value. This un-analysed value
 * can be used for sorting etc and the original field used for text searching.
 * <p>
 * For example, customer name and product name are good candidates for marking with @DocSortable.
 * </p>
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DocSortable {

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
