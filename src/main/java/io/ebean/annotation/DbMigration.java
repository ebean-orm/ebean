package io.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.ebean.Platform;

/**
 * Annotation to specify details for DDL &amp; Migration-generation. (e.g. defaults/renames/...)
 * This annotation is <b>EXPERMIENTAL</b> and may change.
 *
 * @author Roland Praml, FOCONIS AG
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Repeatable(DbMigration.List.class)
public @interface DbMigration {

  /**
   * DdlScripts that will be executed before the 'alter' command.
   *
   * You may write a custom update routine here.
   * If you do not specify an SQL here, and this will alter the table
   * to a non-null column, ebean will autogenerate a statement from
   * default value like this:
   * <pre>
   * UPDATE table SET column = 'foo' WHERE column IS NULL
   * </pre>
   */
  String[] preAlter() default {};

  /**
   * DdlScript that will be executed after the 'alter' command
   */
  String[] postAlter() default {};

  /**
   * DdlScript that will be executed before the 'add' command
   */
  String[] preAdd() default {};

  /**
   * DdlScript that will be executed after the 'add' command.
   * You may write certain update scripts here.
   */
  String[] postAdd() default {};

  // TODO: Do we need preDrop / postDrop?


  Platform[] platforms() default {};

  /**
   * Repeatable support for {@link DbMigration}.
   */
  @Target({ ElementType.FIELD })
  @Retention(RetentionPolicy.RUNTIME)
  @interface List {

    DbMigration[] value() default {};
  }
}
