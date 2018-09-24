package io.ebeaninternal.api.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ebean.bean.EntityBean;
import io.ebean.bean.PersistenceContext;
import io.ebeaninternal.server.deploy.BeanDescriptor;

import java.io.IOException;
import java.util.Map;

public interface SpiJsonReader {

  PersistenceContext getPersistenceContext();

  SpiJsonReader forJson(JsonParser moreJson, boolean resetContext);

  <T> void persistenceContextPut(Object beanId, T currentBean);

  Object persistenceContextPutIfAbsent(Object id, EntityBean bean, BeanDescriptor<?> beanDesc);

  ObjectMapper getObjectMapper();

  JsonParser getParser();

  JsonToken nextToken() throws IOException;

  void pushPath(String path);

  void popPath();

  void beanVisitor(Object bean, Map<String, Object> unmappedProperties);

  Object readValueUsingObjectMapper(Class<?> propertyType) throws IOException;
}
