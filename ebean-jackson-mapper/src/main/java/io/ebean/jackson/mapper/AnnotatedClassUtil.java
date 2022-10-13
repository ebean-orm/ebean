package io.ebean.jackson.mapper;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedClassResolver;

/**
 * Used to obtain the Jackson AnnotatedClass for a given bean type utlimately to obtain field level Jackson annotations.
 */
final class AnnotatedClassUtil {

  /**
   * Return the Jackson AnnotatedClass for the given bean type.
   */
  static AnnotatedClass obtain(ObjectMapper objectMapper, Class<?> beanType) {
    JavaType javaType = objectMapper.getTypeFactory().constructType(beanType);
    return AnnotatedClassResolver.resolve(objectMapper.getDeserializationConfig(), javaType, objectMapper.getDeserializationConfig());
  }
}
