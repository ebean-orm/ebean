package com.avaje.ebean.text.json;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Provides the ability to customise the reading of JSON content.
 * <p>
 * You can optionally provide a custom JsonValueAdapter to handle specific
 * formatting for Date and DateTime types.
 * </p>
 * <p>
 * You can optionally register JsonReadBeanVisitors to customise the processing
 * of the beans as they are processed and handle any custom JSON elements that
 * could not be mapped to bean properties.
 * </p>
 * 
 * @author rbygrave
 * 
 */
public class JsonReadOptions {

  protected JsonValueAdapter valueAdapter;

  protected Map<String, JsonReadBeanVisitor<?>> visitorMap;

  /**
   * Default constructor.
   */
  public JsonReadOptions() {
    this.visitorMap = new LinkedHashMap<String, JsonReadBeanVisitor<?>>();
  }

  /**
   * Return the JsonValueAdapter.
   */
  public JsonValueAdapter getValueAdapter() {
    return valueAdapter;
  }

  /**
   * Return the map of JsonReadBeanVisitor's.
   */
  public Map<String, JsonReadBeanVisitor<?>> getVisitorMap() {
    return visitorMap;
  }

  /**
   * Set a JsonValueAdapter for custom DateTime and Date formatting.
   */
  public JsonReadOptions setValueAdapter(JsonValueAdapter valueAdapter) {
    this.valueAdapter = valueAdapter;
    return this;
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

}
