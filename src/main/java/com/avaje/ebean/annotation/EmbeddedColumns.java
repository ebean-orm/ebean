package com.avaje.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specify property name to db column mapping for Embedded beans.
 * <p>
 * This is designed to be easier to use than the AttributeOverride annotation in
 * standard JPA.
 * </p>
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface EmbeddedColumns {

  /**
   * A list of property names mapped to DB columns.
   * <p>
   * For example <code>currency=IN_CURR, amount=IN_AMOUNT</code>
   * </p>
   * <p>
   * Where currency and amount are properties and IN_CURR and IN_AMOUNT are the
   * respective DB columns these properties will be mapped to.
   * </p>
   */
  String columns() default "";

}
