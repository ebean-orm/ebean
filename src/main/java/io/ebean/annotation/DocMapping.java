package io.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specify the entity type maps to a document store (like ElasticSearch).
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DocMapping {

  /**
   * The property name the mapping applies to.
   */
  String name();

  /**
   * Mapping options.
   */
  DocProperty options();
}
