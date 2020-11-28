package io.ebeaninternal.server.deploy.meta;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedClassResolver;
import io.ebean.config.DatabaseConfig;

/**
 * Used to obtain the Jackson AnnotatedClass for a given bean type utlimately to obtain field level Jackson annotations.
 */
class DeployBeanObtainJackson<T> {

  private final DatabaseConfig config;
  private final Class<T> beanType;

  DeployBeanObtainJackson(DatabaseConfig config, Class<T> beanType) {
    this.config = config;
    this.beanType = beanType;
  }

  /**
   * Return the Jackson AnnotatedClass for the given bean type.
   */
  Object obtain() {
    ObjectMapper objectMapper = (ObjectMapper) config.getObjectMapper();
    JavaType javaType = objectMapper.getTypeFactory().constructType(beanType);
    return AnnotatedClassResolver.resolve(objectMapper.getDeserializationConfig(), javaType, objectMapper.getDeserializationConfig());
  }
}
