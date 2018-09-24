package io.ebean.config.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Manages the underlying map of properties we are gathering.
 */
class LoadContext {

  private static final Logger log = LoggerFactory.getLogger(LoadContext.class);

  /**
   * Map we are loading the properties into.
   */
  private final Map<String, String> map = new LinkedHashMap<>();

  /**
   * Names of resources/files that were loaded.
   */
  private final Set<String> loadedResources = new LinkedHashSet<>();

  /**
   * Return the input stream (maybe null) for the given source.
   */
  InputStream resource(String resourcePath, Loader.Source source) {

    InputStream is = null;
    if (source == Loader.Source.RESOURCE) {
      is = resourceStream(resourcePath);
      if (is != null) {
        loadedResources.add(resourcePath);
      }
    } else {
      File file = new File(resourcePath);
      if (file.exists()) {
        try {
          is = new FileInputStream(file);
          loadedResources.add("file:" + resourcePath);
        } catch (FileNotFoundException e) {
          throw new IllegalStateException(e);
        }
      }
    }

    return is;
  }

  private InputStream resourceStream(String resourcePath) {
    InputStream is = getClass().getResourceAsStream("/" + resourcePath);
    if (is == null) {
      // search the module path for top level resource
      is = ClassLoader.getSystemResourceAsStream(resourcePath);
    }
    return is;
  }

  /**
   * Add a property entry.
   */
  void put(String key, String val) {
    if (val != null) {
      val = val.trim();
    }
    map.put(key, val);
  }

  /**
   * Evaluate all the expressions and return as a Properties object.
   */
  Properties eval() {

    log.info("loaded properties from {}", loadedResources);

    Properties properties = new Properties();

    for (Map.Entry<String, String> entry : map.entrySet()) {
      String key = entry.getKey();
      String value = PropertyEval.eval(entry.getValue());
      properties.setProperty(key, value);
    }

    return properties;
  }

  /**
   * Read the special properties that can point to an external properties source.
   */
  String indirectLocation() {
    String indirectLocation = map.get("load.properties");
    if (indirectLocation == null) {
      indirectLocation = map.get("load.properties.override");
    }
    return indirectLocation;
  }
}
