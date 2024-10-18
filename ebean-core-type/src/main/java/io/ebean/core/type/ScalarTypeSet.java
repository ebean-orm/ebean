package io.ebean.core.type;

import org.jspecify.annotations.Nullable;

/**
 * A ScalarType that has variations based on the mapped JDBC type (like VARCHAR, CLOB, JSON etc).
 */
public interface ScalarTypeSet<T> {

  /**
   * The property type these scalar types map to.
   */
  Class<?> type();

  /**
   * Return a default ScalarType to use when no other annotations like {@code @DbJson} are present.
   */
  @Nullable
  ScalarType<?> defaultType();

  /**
   * Return the scalarType to use for the given jdbc type.
   * <p>
   * For example VARCHAR, CLOB, JSON etc.
   */
  ScalarType<T> forType(int jdbcType);

}
