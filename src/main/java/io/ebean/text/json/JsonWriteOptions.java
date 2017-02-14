package io.ebean.text.json;

import io.ebean.FetchPath;
import io.ebean.config.JsonConfig;
import io.ebean.text.PathProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides options for customising the JSON write process.
 * <p>
 * You can explicitly state which properties to include in the JSON output for
 * the root level and each path.
 * </p>
 */
public class JsonWriteOptions {

  protected FetchPath pathProperties;

  protected Object objectMapper;

  protected JsonConfig.Include include;

  protected Map<String, JsonWriteBeanVisitor<?>> visitorMap;

  /**
   * Parse and return a PathProperties from nested string format like
   * (a,b,c(d,e),f(g)) where "c" is a path containing "d" and "e" and "f" is a
   * path containing "g" and the root path contains "a","b","c" and "f".
   *
   * @see io.ebean.text.PathProperties#parse(String)
   */
  public static JsonWriteOptions parsePath(String pathProperties) {

    return pathProperties(PathProperties.parse(pathProperties));
  }

  /**
   * Construct JsonWriteOptions with the given pathProperties.
   */
  public static JsonWriteOptions pathProperties(FetchPath pathProperties) {
    JsonWriteOptions o = new JsonWriteOptions();
    o.setPathProperties(pathProperties);
    return o;
  }

  /**
   * Set the Map of properties to include by path.
   */
  public void setPathProperties(FetchPath pathProperties) {
    this.pathProperties = pathProperties;
  }

  /**
   * Return the properties to include by path.
   */
  public FetchPath getPathProperties() {
    return pathProperties;
  }

  /**
   * Return the include mode for this request.
   */
  public JsonConfig.Include getInclude() {
    return include;
  }

  /**
   * Set the include mode for this request.
   */
  public void setInclude(JsonConfig.Include include) {
    this.include = include;
  }

  /**
   * Register a JsonWriteBeanVisitor for the root level.
   */
  public JsonWriteOptions setRootPathVisitor(JsonWriteBeanVisitor<?> visitor) {
    return setPathVisitor(null, visitor);
  }

  /**
   * Register a JsonWriteBeanVisitor for the given path.
   */
  public JsonWriteOptions setPathVisitor(String path, JsonWriteBeanVisitor<?> visitor) {
    if (visitorMap == null) {
      visitorMap = new HashMap<>();
    }
    visitorMap.put(path, visitor);
    return this;
  }

  /**
   * Return the Map of registered JsonWriteBeanVisitor's by path.
   */
  public Map<String, JsonWriteBeanVisitor<?>> getVisitorMap() {
    return visitorMap;
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
