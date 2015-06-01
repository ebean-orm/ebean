package com.avaje.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specify the update mode for the specific entity type.
 * <p>
 * Control whether all 'loaded' properties are included in an Update or whether
 * just properties that have changed will be included in the update.
 * </p>
 * <p>
 * Note that the default can be set via ebean.properties.
 * </p>
 * 
 * <pre>
 * ## Set to update all loaded properties
 * ebean.updateChangesOnly=false
 * </pre>
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface UpdateMode {

  /**
   * Set to false if you want to include all the 'loaded' properties in the
   * update. Otherwise, just the properties that have changed will be included
   * in the update.
   */
  boolean updateChangesOnly() default true;

}
