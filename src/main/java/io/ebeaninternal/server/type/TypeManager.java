package io.ebeaninternal.server.type;

import io.ebean.annotation.DbArray;

import javax.persistence.EnumType;
import java.lang.reflect.Type;

/**
 * Convert an Object to the required type.
 */
public interface TypeManager {

  /**
   * Register a ScalarType with the system.
   */
  void add(ScalarType<?> scalarType);

  /**
   * Register a ScalarType for an Enum with can have multiple classes.
   */
  @SuppressWarnings("rawtypes")
  void addEnumType(ScalarType<?> type, Class<? extends Enum> myEnumClass);

  /**
   * Return the scalar type for the given logical type.
   */
  ScalarType<?> getScalarType(String cast);

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
  ScalarType<?> createEnumScalarType(Class<? extends Enum<?>> enumType, EnumType enumerated);

  /**
   * Return the ScalarType used to handle JSON content.
   * <p>
   * Note that type expected to be JsonNode or Map.
   * </p>
   */
  ScalarType<?> getJsonScalarType(Class<?> type, int dbType, int dbLength, Type genericType);

  /**
   * Return the ScalarType used to handle DB ARRAY.
   */
  ScalarType<?> getArrayScalarType(Class<?> type, DbArray dbArray, Type genericType);

  /**
   * Return the ScalarType used to handle HSTORE (Map<String,String>).
   */
  ScalarType<?> getHstoreScalarType();
}
