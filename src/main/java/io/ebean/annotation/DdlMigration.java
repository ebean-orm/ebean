package io.ebean.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.ebeaninternal.server.deploy.parse.AnnotationParser;



/**
 * Annotate an entity bean with &#64;CustomAnnotationParser and specify a {@link AnnotationParser} class that is invoked on
 * post process. This class can post-process the DeployBeanDescriptor and generate custom Formulas e.g. 
 * 
 * FIXME: AnnotationParser is ebeaninternal and should be accessed through (not yet existing) interfaces.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Repeatable(DdlMigration.List.class)
public @interface DdlMigration {

  /**
   * The DefaultValue
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