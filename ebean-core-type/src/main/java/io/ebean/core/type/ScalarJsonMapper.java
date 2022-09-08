package io.ebean.core.type;

/**
 * Supports JSON bean properties that use something like Jackson ObjectMapper to convert between
 * JSON content and the logical bean property value.
 */
public interface ScalarJsonMapper {

  /**
   * Return a ScalarType for the given JSON property.
   */
  ScalarType<?> createType(ScalarJsonRequest request);
}
