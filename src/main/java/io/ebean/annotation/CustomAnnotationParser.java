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
@Target(ElementType.TYPE)
@Repeatable(CustomAnnotationParser.List.class)
public @interface CustomAnnotationParser {

  public enum Stage {
    INITIAL, XML_MAPPING, EMBEDDED_DEPLOYMENT, BEAN_TABLE, DEPLOYMENT_ASSOCIATIONS, ID_GENERATORS, RELATIONSHIPS
  }
  /**
   * The CustomDeployParser classes
   */
  Class<? extends AnnotationParser> value();
  
  Stage stage() default Stage.ID_GENERATORS;
  
  /**
   * Defines several {@link CustomAnnotationParser} annotations on the same element.
   */
  @Target({ ElementType.TYPE })
  @Retention(RUNTIME)
  @Documented
  @interface List {

    CustomAnnotationParser[] value();
  }

}