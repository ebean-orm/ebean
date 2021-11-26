package io.ebean.text.json;

import com.fasterxml.jackson.core.JsonParser;
import io.ebean.bean.PersistenceContext;

/**
 * Provides a JSON reader that can hold a persistence context and load context while reading JSON.
 * <p>
 * This provides a mechanism such that an object loaded from JSON can have unique instances
 * by using a persistence context and also support further lazy loading (via a load context).
 * </p>
 */
public interface JsonBeanReader<T> {

  /**
   * Read the JSON returning a bean.
   */
  T read();

  /**
   * Create a new reader taking the context from the existing one but using a new JsonParser.
   */
  JsonBeanReader<T> forJson(JsonParser moreJson);

  /**
   * Add a bean explicitly to the persistence context.
   */
  void persistenceContextPut(Object beanId, T currentBean);

  /**
   * Return the persistence context if one is being used.
   */
  PersistenceContext getPersistenceContext();

}
