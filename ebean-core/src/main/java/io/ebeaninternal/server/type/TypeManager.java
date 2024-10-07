package io.ebeaninternal.server.type;

import io.ebean.core.type.ScalarType;
import io.ebeaninternal.server.deploy.meta.DeployProperty;

import jakarta.persistence.EnumType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Convert an Object to the required type.
 */
public interface TypeManager {

  /**
   * Return the scalar type for the given logical type.
   */
  ScalarType<?> type(String cast);

  /**
   * Return the ScalarType for a given jdbc type.
   *
   * @param jdbcType as per java.sql.Types
   */
  ScalarType<?> type(int jdbcType);

  /**
   * Return the ScalarType for a given logical type.
   */
  ScalarType<?> type(Class<?> type);

  /**
   * For java.util.Date and java.util.Calendar additionally pass the jdbc type
   * that you would like the ScalarType to map to. This is because these types
   * can map to different java.sql.Types depending on the property.
   */
  ScalarType<?> type(Class<?> type, int jdbcType);

  /**
   * Find and return the ScalarType taking into account the property type with generics.
   * <p>
   * For example Array based ScalarType for types like {@code List<String>}.
   */
  ScalarType<?> type(DeployProperty property);

  /**
   * Create a ScalarType for an Enum using a mapping (rather than JPA Ordinal or String which has limitations).
   */
  ScalarType<?> enumType(Class<? extends Enum<?>> enumType, EnumType enumerated);

  /**
   * Returns the Json Marker annotation (e.g. JacksonAnnotation)
   */
  Class<? extends Annotation> jsonMarkerAnnotation();

  /**
   * Return the ScalarType used to handle JSON content.
   * <p>
   * Note that type expected to be JsonNode or Map.
   * </p>
   */
  ScalarType<?> dbJsonType(DeployProperty prop, int dbType, int dbLength);

  /**
   * Return the ScalarType used to handle DB ARRAY.
   */
  ScalarType<?> dbArrayType(Class<?> type, Type genericType, boolean nullable);

  /**
   * Return the ScalarType used to handle HSTORE (Map<String,String>).
   */
  ScalarType<?> dbMapType();

  /**
   * Return the Geometry type binder if provided.
   */
  GeoTypeBinder geoTypeBinder();
}
