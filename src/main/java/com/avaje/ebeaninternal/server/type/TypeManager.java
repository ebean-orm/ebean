package com.avaje.ebeaninternal.server.type;

import com.avaje.ebeaninternal.server.type.reflect.CheckImmutableResponse;

/**
 * Convert an Object to the required type.
 */
public interface TypeManager {

  /**
   * Check if the type is immutable using reflection.
   */
  CheckImmutableResponse checkImmutable(Class<?> cls);

  /**
   * Create ScalarDataReader's for the Immutable compound type.
   */
  ScalarDataReader<?> recursiveCreateScalarDataReader(Class<?> cls);

  /**
   * Create ScalarTypes for this Immutable Value Object type.
   */
  ScalarType<?> recursiveCreateScalarTypes(Class<?> cls);

  /**
   * Register a ScalarType with the system.
   */
  void add(ScalarType<?> scalarType);

  /**
   * Return the Internal CompoundType handler for a given compound type.
   */
  CtCompoundType<?> getCompoundType(Class<?> type);

  /**
   * Return the ScalarType for a given jdbc type.
   *
   * @param jdbcType as per java.sql.Types
   */
  ScalarType<?> getScalarType(int jdbcType);

  /**
   * Return the ScalarType for a given logical type.
   */
  ScalarType<?> getScalarType(Class<?> type);

  /**
   * For java.util.Date and java.util.Calendar additionally pass the jdbc type
   * that you would like the ScalarType to map to. This is because these types
   * can map to different java.sql.Types depending on the property.
   */
  ScalarType<?> getScalarType(Class<?> type, int jdbcType);

  /**
   * Create a ScalarType for an Enum using a mapping (rather than JPA Ordinal
   * or String which has limitations).
   */
  ScalarType<?> createEnumScalarType(Class<? extends Enum<?>> enumType);

  /**
   * Return the ScalarType used to handle JSON content.
   * <p>
   * Note that type expected to be JsonNode or Map.
   * </p>
   */
  ScalarType<?> getJsonScalarType(Class<?> type, int dbType);
}
