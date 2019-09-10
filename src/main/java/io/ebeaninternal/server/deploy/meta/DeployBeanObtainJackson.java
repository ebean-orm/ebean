package io.ebeaninternal.server.deploy.meta;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedClassResolver;
import io.ebean.config.ServerConfig;

/**
 * Used to obtain the Jackson AnnotatedClass for a given bean type utlimately to obtain field level Jackson annotations.
 */
class DeployBeanObtainJackson<T> {

  private final ServerConfig serverConfig;
  private final Class<T> beanType;

  DeployBeanObtainJackson(ServerConfig serverConfig, Class<T> beanType) {
    this.serverConfig = serverConfig;
    this.beanType = beanType;
  }

  /**
   * Return the Jackson AnnotatedClass for the given bean type.
   */
  Object obtain() {

    ObjectMapper objectMapper = (ObjectMapper) serverConfig.getObjectMapper();
    JavaType javaType = objectMapper.getTypeFactory().constructType(beanType);
    return AnnotatedClassResolver.resolve(objectMapper.getDeserializationConfig(), javaType, objectMapper.getDeserializationConfig());
  }
}
