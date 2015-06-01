package com.avaje.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * For mapping the values of an Enum to and from Database values.
 * <p>
 * Also refer to the {@link EnumValue} approach which probably the preferred now
 * (preferred over using this EnumMapping annotation).
 * </p>
 * <p>
 * Both of the approaches defined in the JPA have significant problems!!!
 * </p>
 * <p>
 * Using the ordinal value is VERY RISKY because that depends on the compile
 * order of the enum values. Aka if you change the order of the enum values you
 * have changed their ordinal values and now your DB values are WRONG - a HUGE
 * disaster!!!.
 * </p>
 * <p>
 * Using the String values of enums is fairly restrictive because in a Database
 * these values are usually truncated into short codes (e.g. "A" short for
 * "ACTIVE") so space used in the database is minimised. Making your enum names
 * match the database values would give them very short less meaningful names -
 * not a great solution.
 * </p>
 * <p>
 * You can use this annotation to control the mapping of your enums to database
 * values.
 * </p>
 * <p>
 * The design of this using nameValuePairs is not optimal for safety or
 * refactoring so if you have a better solution I'm all ears. The other
 * solutions would probably involve modifying each enumeration with a method
 * which may be ok.
 * </p>
 * <p>
 * An example mapping the UserState enum.
 * </p>
 * 
 * <pre class="code">
 * ...
 * &#064;EnumMapping(nameValuePairs=&quot;NEW=N, ACTIVE=A, INACTIVE=I&quot;)
 *  public enum UserState {
 *  NEW,
 *  ACTIVE,
 *  INACTIVE;
 *  }
 * </pre>
 * 
 * @see EnumValue
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumMapping {

  /**
   * A comma delimited list of name=value pairs.
   * <p>
   * e.g. "ACTIVE=A, INACTIVE=I, NEW=N".
   * </p>
   * <p>
   * Where ACTIVE, INACTIVE and NEW are the enumeration values and "A", "I" and
   * "N" are the database values.
   * </p>
   * <p>
   * This is not really an optimal approach so if you have a better one I'm all
   * ears - thanks.
   * </p>
   */
  String nameValuePairs();

  /**
   * Defaults to mapping values to database VARCHAR type. If this is set to true
   * then the values will be converted to INTEGER and mapped to the database
   * integer type.
   * <p>
   * e.g. "ACTIVE=1, INACTIVE=0, NEW=2".
   * </p>
   */
  boolean integerType() default false;

  /**
   * The length of DB column if mapping to string values.
   */
  int length() default 0;
}
