package com.avaje.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specify that the property is stored in encrypted form.
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Encrypted {

  /**
   * When true try to use DB encryption rather than local java encryption.
   */
  boolean dbEncryption() default true;

  /**
   * Used to specify the DB column length.
   */
  int dbLength() default 0;
}
