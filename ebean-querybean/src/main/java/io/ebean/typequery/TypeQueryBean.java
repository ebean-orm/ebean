package io.ebean.typequery;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to denote a type query bean.
 * <p>
 * These are typically generated beans used to build queries using type safe query criteria.
 * </p>
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface TypeQueryBean {

  /**
   * The version description for the query bean.
   */
  String value() default "v0";
}
