package com.avaje.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specify explicit sql for multiple select statements. Need to use this if you
 * have more than one SqlSelect for a given bean.
 * <p>
 * FUTURE: Support explicit sql for SqlInsert, SqlUpdate and SqlDelete.
 * </p>
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Sql {

  /**
   * The sql select statements.
   */
  SqlSelect[] select() default { @SqlSelect };

}
