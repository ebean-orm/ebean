package com.avaje.ebean.config;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

/**
 * A map like structure of properties.
 * <p/>
 * Handles evaluation of expressions like ${home} and provides convenience methods for int, long and boolean.
 */
public final class PropertyMap implements Serializable {

  private static final long serialVersionUID = 1L;

  private LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();

  public static Properties defaultProperties() {
    PropertyMap propertyMap = PropertyMapLoader.loadGlobalProperties();
    return (propertyMap == null) ? new Properties() : propertyMap.asProperties();
  }

  public String toString() {
    return map.toString();
  }

  /**
   * Return as standard Properties.
   */
  public Properties asProperties() {
    Properties properties = new Properties();
    for (Entry<String, String> e : entrySet()) {
      properties.put(e.getKey(), e.getValue());
    }
    return properties;
  }

  /**
   * Go through all the properties and evaluate any expressions that have not
   * been resolved.
   */
  public void evaluateProperties() {

    for (Entry<String, String> e : entrySet()) {
      String key = e.getKey();
      String val = e.getValue();
      String eval = eval(val);
      if (eval != null && !eval.equals(val)) {
        put(key, eval);
      }
    }
  }

  /**
   * Returns the value with expressions like ${home} evaluated using system properties and environment variables.
   */
  public synchronized String eval(String val) {
    return PropertyExpression.eval(val, this);
  }

  /**
   * Return the boolean property value with a given default.
   */
  public synchronized boolean getBoolean(String key, boolean defaultValue) {
    String value = get(key);
    if (value == null) {
      return defaultValue;
    } else {
      return Boolean.parseBoolean(value);
    }
  }

  /**
   * Return the int property value with a given default.
   */
  public synchronized int getInt(String key, int defaultValue) {
    String value = get(key);
    if (value == null) {
      return defaultValue;
    } else {
      return Integer.parseInt(value);
    }
  }

  /**
   * Return the long property value with a given default.
   */
  public synchronized long getLong(String key, long defaultValue) {
    String value = get(key);
    if (value == null) {
      return defaultValue;
    } else {
      return Long.parseLong(value);
    }
  }

  /**
   * Return the string property value with a given default.
   */
  public synchronized String get(String key, String defaultValue) {
    String value = map.get(key.toLowerCase());
    return value == null ? defaultValue : value;
  }

  /**
   * Return the property value returning null if there is no value defined.
   */
  public synchronized String get(String key) {
    return map.get(key.toLowerCase());
  }

  /**
   * Put all evaluating any expressions in the values.
   */
  public synchronized void putEvalAll(Map<String, String> keyValueMap) {

    for (Map.Entry<String, String> entry : keyValueMap.entrySet()) {
      putEval(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Put a single key value evaluating any expressions in the value.
   */
  public synchronized String putEval(String key, String value) {
    value = PropertyExpression.eval(value, this);
    return map.put(key.toLowerCase(), value);
  }

  /**
   * Put a single key value with no expression evaluation.
   */
  public synchronized String put(String key, String value) {
    return map.put(key.toLowerCase(), value);
  }

  /**
   * Remove an entry.
   */
  public synchronized String remove(String key) {
    return map.remove(key.toLowerCase());
  }

  /**
   * Return the entries.
   */
  public synchronized Set<Entry<String, String>> entrySet() {
    return map.entrySet();
  }

}
