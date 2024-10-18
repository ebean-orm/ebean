package io.ebean.core.type;

import org.jspecify.annotations.Nullable;

import java.lang.annotation.Annotation;

/**
 * Supports JSON bean properties that use something like Jackson ObjectMapper to convert between
 * JSON content and the logical bean property value.
 */
public interface ScalarJsonMapper {

  /**
   * Return a ScalarType for the given JSON property.
   */
  ScalarType<?> createType(ScalarJsonRequest request);

  /**
   * Return a marker annotation to detect when the JSON mapper should be used explicitly.
   */
  @Nullable
  <A extends Annotation> Class<A> markerAnnotation();
}
