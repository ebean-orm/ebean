package io.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify details for DDL &amp; Migration-generation. (e.g. defaults/renames/...)
 * This annotation is <b>EXPERMIENTAL</b> and may change.
 * 
 * @author Roland Praml, FOCONIS AG
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DdlInfo {
  public static final String UNSET = "__UNSET__";
  /**
   * The defaultValue for new non-null columns.
   */
  String defaultValue() default UNSET;
  
  
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
  DdlScript[] preAlter() default {};
  
  /**
   * DdlScript that will be executed after the 'alter' command
   */
  DdlScript[] postAlter() default {};
  
  /**
   * DdlScript that will be executed before the 'add' command
   */
  DdlScript[] preAdd() default {};
  
  /**
   * DdlScript that will be executed after the 'add' command.
   * You may write certain update scripts here.
   */
  DdlScript[] postAdd() default {};
  
  // TODO: Do we need preDrop / postDrop?

}