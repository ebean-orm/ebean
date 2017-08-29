package io.ebean.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import io.ebean.Platform;

/**
 * Annotation to specify a script that shoud be applied during ddl generation or migration
 * 
 * @author Roland Praml, FOCONIS AG
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DdlScript {
  
  /**
   * SQL that will be executed added to the ddl generation or migration scripts
   */
  String value();
  
  /**
   * Specify for which platforms this DdlMigration takes place.
   * If platforms is empty, this means, this script is applied to all platforms.
   */
  Platform[] platforms() default {};

  /**
   * Specify for which platforms this DdlScript is excluded.
   * It makes only sense to specify platforms or excludePlatforms.
   */
  Platform[] excludePlatforms() default {};

}