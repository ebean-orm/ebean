package io.ebeaninternal.server.type;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedClassResolver;

/**
 * Used to obtain the Jackson AnnotatedClass for a given bean type utlimately to obtain field level Jackson annotations.
 */
final class DeployBeanObtainJackson<T> {

  private final ObjectMapper objectMapper;
  private final Class<T> beanType;

  DeployBeanObtainJackson(ObjectMapper objectMapper, Class<T> beanType) {
    this.objectMapper = objectMapper;
    this.beanType = beanType;
  }

  /**
   * Return the Jackson AnnotatedClass for the given bean type.
   */
  AnnotatedClass obtain() {
    JavaType javaType = objectMapper.getTypeFactory().constructType(beanType);
    return AnnotatedClassResolver.resolve(objectMapper.getDeserializationConfig(), javaType, objectMapper.getDeserializationConfig());
  }
}
