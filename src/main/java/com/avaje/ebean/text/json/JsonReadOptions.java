package com.avaje.ebean.text.json;

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

  /**
   * Default constructor.
   */
  public JsonReadOptions() {
    this.visitorMap = new LinkedHashMap<String, JsonReadBeanVisitor<?>>();
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
   * Return the Jackson ObjectMapper to use (if not wanted to use the objectMapper set on the ServerConfig).
   */
  public Object getObjectMapper() {
    return objectMapper;
  }

  /**
   * Set the Jackson ObjectMapper to use (if not wanted to use the objectMapper set on the ServerConfig).
   */
  public void setObjectMapper(Object objectMapper) {
    this.objectMapper = objectMapper;
  }
}