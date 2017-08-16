package io.ebean.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;



/**
 * Annotation to specify details for DDL-migration generation. (e.g. defaults/renames/...)
 * This annotation is <b>EXPERMIENTAL</b> and may change.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Repeatable(DdlMigration.List.class)
public @interface DdlMigration {

  /**
   * The defaultValue for new non-null columns.
   */
  String defaultValue() default "";
  
  /**
   * Specify the DDL version here (this is mainly for documentation)
   */
  String since() default "";
  
  /**
   * Defines several {@link DdlMigration} annotations on the same element.
   */
  @Target(ElementType.FIELD)
  @Retention(RUNTIME)
  @Documented
  @interface List {

    DdlMigration[] value();
  }

}