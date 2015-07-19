package com.avaje.ebean.text.json;

import com.avaje.ebean.text.PathProperties;

/**
 * Provides options for customising the JSON write process.
 * <p>
 * You can explicitly state which properties to include in the JSON output for
 * the root level and each path.
 * </p>
 */
public class JsonWriteOptions {

  protected PathProperties pathProperties;

  protected Object objectMapper;

  /**
   * Parse and return a PathProperties from nested string format like
   * (a,b,c(d,e),f(g)) where "c" is a path containing "d" and "e" and "f" is a
   * path containing "g" and the root path contains "a","b","c" and "f".
   *
   * @see com.avaje.ebean.text.PathProperties#parse(String)
   */
  public static JsonWriteOptions parsePath(String pathProperties) {

    return pathProperties(PathProperties.parse(pathProperties));
  }

  /**
   * Construct JsonWriteOptions with the given pathProperties.
   */
  public static JsonWriteOptions pathProperties(PathProperties pathProperties) {
    JsonWriteOptions o = new JsonWriteOptions();
    o.setPathProperties(pathProperties);
    return o;
  }

  /**
   * Set the Map of properties to include by path.
   */
  public void setPathProperties(PathProperties pathProperties) {
    this.pathProperties = pathProperties;
  }

  /**
   * Return the properties to include by path.
   */
  public PathProperties getPathProperties() {
    return pathProperties;
  }

  /**
   * Return the jackson object mapper to use.
   * <p/>
   * If null the ObjectMapper from serverConfig will be used.
   */
  public Object getObjectMapper() {
    return objectMapper;
  }

  /**
   * Set the jackson object mapper to use.
   * <p/>
   * If null the ObjectMapper from serverConfig will be used.
   */
  public void setObjectMapper(Object objectMapper) {
    this.objectMapper = objectMapper;
  }
}
