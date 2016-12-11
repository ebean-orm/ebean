package io.ebean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specify a method on an Enum that returns the value that should be stored in the DB.
 * <p>
 * This is the preferred option for mapping Enum's to DB values (preferred over the JPA
 * standard @Enumerated and Ebean's @EnumValue annotations).
 * </p>
 * <h3>Example:</h3>
 * <pre>{@code
 *
 *   public enum Status {
 *     NEW("N"),
 *     ACTIVE("A"),
 *     INACTIVE("I");
 *
 *     String dbValue;
 *     Status(String dbValue) {
 *       this.dbValue = dbValue;
 *     }
 *
 *     @DbEnumValue
 *     public String getValue() {
 *       return dbValue;
 *     }
 *   }
 *
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DbEnumValue {

  /**
   * Specify the database type used to store the values (VARCHAR or INTEGER).
   */
  DbEnumType storage() default DbEnumType.VARCHAR;

}
