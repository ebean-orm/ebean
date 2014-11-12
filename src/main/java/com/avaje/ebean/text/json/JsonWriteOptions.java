package com.avaje.ebean.text.json;

import com.avaje.ebean.text.PathProperties;

/**
 * Provides options for customising the JSON write process.
 * <p>
 * You can explicitly state which properties to include in the JSON output for
 * the root level and each path.
 * </p>
 * 
 * <pre class="code">

 * // output as a JSON string with pretty formatting
 * String s = json.toJson(list, true, writeOptions);
 * 
 * </pre>
 */
public class JsonWriteOptions {

  protected PathProperties pathProperties;

  /**
   * Parse and return a PathProperties from nested string format like
   * (a,b,c(d,e),f(g)) where "c" is a path containing "d" and "e" and "f" is a
   * path containing "g" and the root path contains "a","b","c" and "f".
   */
  public static JsonWriteOptions parsePath(String pathProperties) {

    PathProperties p = PathProperties.parse(pathProperties);
    JsonWriteOptions o = new JsonWriteOptions();
    o.setPathProperties(p);
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

}
