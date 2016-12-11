package io.ebean.text.json;

import io.ebean.bean.PersistenceContext;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Provides the ability to customise the reading of JSON content.
 * <p>
 * You can register JsonReadBeanVisitors to customise the processing of the
 * beans as they are processed and handle any custom JSON elements that
 * could not be mapped to bean properties.
 * </p>
 */
public class JsonReadOptions {

  protected final Map<String, JsonReadBeanVisitor<?>> visitorMap;

  protected Object objectMapper;

  protected boolean enableLazyLoading;

  protected PersistenceContext persistenceContext;

  protected Object loadContext;

  /**
   * Default constructor.
   */
  public JsonReadOptions() {
    this.visitorMap = new LinkedHashMap<>();
  }

  /**
   * Return the map of JsonReadBeanVisitor's.
   */
  public Map<String, JsonReadBeanVisitor<?>> getVisitorMap() {
    return visitorMap;
  }

  /**
   * Register a JsonReadBeanVisitor for the root level.
   */
  public JsonReadOptions addRootVisitor(JsonReadBeanVisitor<?> visitor) {
    return addVisitor(null, visitor);
  }

  /**
   * Register a JsonReadBeanVisitor for a given path.
   */
  public JsonReadOptions addVisitor(String path, JsonReadBeanVisitor<?> visitor) {
    visitorMap.put(path, visitor);
    return this;
  }

  /**
   * Return true if lazy loading is enabled after the objects are loaded.
   */
  public boolean isEnableLazyLoading() {
    return enableLazyLoading;
  }

  /**
   * Set to true to enable lazy loading on partially populated beans.
   * <p>
   * If this is set to true a persistence context will be created if one has
   * not already been supplied.
   */
  public JsonReadOptions setEnableLazyLoading(boolean enableLazyLoading) {
    this.enableLazyLoading = enableLazyLoading;
    return this;
  }

  /**
   * Return the Jackson ObjectMapper to use (if not wanted to use the objectMapper set on the ServerConfig).
   */
  public Object getObjectMapper() {
    return objectMapper;
  }

  /**
   * Set the Jackson ObjectMapper to use (if not wanted to use the objectMapper set on the ServerConfig).
   */
  public JsonReadOptions setObjectMapper(Object objectMapper) {
    this.objectMapper = objectMapper;
    return this;
  }

  /**
   * Set the persistence context to use when building the object graph from the JSON.
   */
  public JsonReadOptions setPersistenceContext(PersistenceContext persistenceContext) {
    this.persistenceContext = persistenceContext;
    return this;
  }

  /**
   * Return the persistence context to use when marshalling JSON.
   */
  public PersistenceContext getPersistenceContext() {
    return persistenceContext;
  }

  /**
   * Return the load context to use.
   */
  public Object getLoadContext() {
    return loadContext;
  }

  /**
   * Set the load context to use.
   */
  public void setLoadContext(Object loadContext) {
    this.loadContext = loadContext;
  }

}
