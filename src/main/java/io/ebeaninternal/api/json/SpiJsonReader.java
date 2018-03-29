package io.ebeaninternal.api.json;

import io.ebean.bean.EntityBean;
import io.ebean.bean.PersistenceContext;
import io.ebean.text.json.JsonReader;
import io.ebeaninternal.server.deploy.BeanDescriptor;

import java.io.IOException;
import java.util.Map;

public interface SpiJsonReader extends JsonReader {

  PersistenceContext getPersistenceContext();

  <T> void persistenceContextPut(Object beanId, T currentBean);

  Object persistenceContextPutIfAbsent(Object id, EntityBean bean, BeanDescriptor<?> beanDesc);

  void pushPath(String path);

  void popPath();

  void beanVisitor(Object bean, Map<String, Object> unmappedProperties);

  Object readValueUsingObjectMapper(Class<?> propertyType) throws IOException;

}
