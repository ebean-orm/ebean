package com.avaje.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Deprecated in favor of @DocEmbedded (i.e. renamed to @DocEmbedded)
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Deprecated
public @interface DocStoreEmbedded {

  /**
   * The properties on the embedded bean to include in the index.
   */
  String doc() default "";

}
