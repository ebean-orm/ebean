package io.ebeaninternal.api.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonReader.Token;
import io.ebean.bean.EntityBean;
import io.ebean.bean.PersistenceContext;
import io.ebeaninternal.server.deploy.BeanDescriptor;

import java.io.IOException;
import java.util.Map;

public interface SpiJsonReader {

  PersistenceContext persistenceContext();

  SpiJsonReader forJson(JsonReader moreJson);

  SpiJsonReader forJson(String moreJson);

  <T> void persistenceContextPut(Object beanId, T currentBean);

  Object persistenceContextPutIfAbsent(Object id, EntityBean bean, BeanDescriptor<?> beanDesc);

  ObjectMapper mapper();

  JsonReader parser();

  Token nextToken() throws IOException;

  void pushPath(String path);

  void popPath();

  void beanVisitor(Object bean, Map<String, Object> unmappedProperties);

  Object readValueUsingObjectMapper(Class<?> propertyType) throws IOException;

  boolean update();
}
