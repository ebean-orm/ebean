package io.ebeaninternal.server.type;

import javax.persistence.EnumType;
import java.util.Set;

/**
 * Marker interface for the Enum scalar types.
 */
public interface ScalarTypeEnum<T> extends ScalarType<T> {

  /**
   * Return the IN values for DB constraint construction.
   */
  Set<String> getDbCheckConstraintValues();

  /**
   * Return true if we allow this scalar enum type to be overridden.
   * Ability to override the built-in support for java time DayOfWeek and Month.
   */
  default boolean isOverrideBy(EnumType type) {
    return false;
  }

  /**
   * Return true if the scalar type is compatible with the specified enum type.
   */
  boolean isCompatible(EnumType enumType);

}
