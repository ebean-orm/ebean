package io.ebean.typequery;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks generated query bean source code.
 * <p>
 * This is code generated by the query bean generator (annotation processor).
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.CLASS)
public @interface Generated {

  /**
   * The name of the generator used to generate this source.
   */
  String value();

}
