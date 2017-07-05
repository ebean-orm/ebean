package io.ebean.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * Helper used to load the ebean.properties into a PropertyMap.
 */
final class PropertyMapLoader {

  private static final Logger logger = LoggerFactory.getLogger(PropertyMapLoader.class);

  private static final Pattern OTHER_PROPS_REPLACE = Pattern.compile("\\", Pattern.LITERAL);

  /**
   * Load the <code>test-ebean.properties</code>.
   */
  public static PropertyMap loadTestProperties() {
    if (!PropertyMap.loadTestProperties()) {
      return new PropertyMap();
    }
    return load(null, "test-ebean.properties");
  }

  /**
   * Load the ebean.properties (and test-ebean.properties if present).
   */
  public static PropertyMap loadGlobalProperties() {

    boolean loadTestProperties = false;
    String fileName = System.getenv("EBEAN_PROPS_FILE");
    if (fileName == null) {
      fileName = System.getProperty("ebean.props.file");
      if (fileName == null) {
        loadTestProperties = PropertyMap.loadTestProperties();
        fileName = "ebean.properties";
      }
    }

    PropertyMap map = load(null, fileName);
    if (loadTestProperties) {
      // load test properties if present in classpath
      map = load(map, "test-ebean.properties");
    }
    return map;
  }

  /**
   * Load the file returning the property map.
   *
   * @param p        an existing property map to load into.
   * @param fileName the name of the properties file to load.
   */
  public static PropertyMap load(PropertyMap p, String fileName) {

    InputStream is = findInputStream(fileName);
    if (is == null) {
      return p;
    } else {
      return load(p, is);
    }
  }

  /**
   * Load the InputStream returning the property map.
   *
   * @param p  an existing property map to load into.
   * @param in the InputStream of the properties file to load.
   */
  public static PropertyMap load(PropertyMap p, InputStream in) {

    Properties props = new Properties();
    try {
      props.load(in);
      in.close();
      return load(p, props);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static PropertyMap load(PropertyMap p, Properties props) {

    if (p == null) {
      p = new PropertyMap();
    }

    // put values in initially without any evaluation
    for (Map.Entry<Object, Object> entry : props.entrySet()) {
      String key = ((String) entry.getKey()).toLowerCase();
      String val = ((String) entry.getValue());
      if (val != null) {
        val = val.trim();
      }
      p.put(key, val);
    }

    p.evaluateProperties();

    String otherProps = p.remove("load.properties");
    if (otherProps == null) {
      otherProps = p.remove("load.properties.override");
    }
    if (otherProps != null) {
      otherProps = OTHER_PROPS_REPLACE.matcher(otherProps).replaceAll(Matcher.quoteReplacement("/"));
      InputStream is = findInputStream(otherProps);
      if (is != null) {
        logger.debug("loading properties from {}", otherProps);
        load(p, is);
      } else {
        logger.error("load.properties " + otherProps + " not found.");
      }
    }

    return p;
  }

  /**
   * Find the input stream given the file name.
   */
  private static InputStream findInputStream(String fileName) {

    if (fileName == null) {
      throw new NullPointerException("fileName is null?");
    }

    try {
      File f = new File(fileName);

      if (f.exists()) {
        logger.debug("{} found in file system", fileName);
        return new FileInputStream(f);
      } else {
        InputStream in = findInClassPath(fileName);
        if (in != null) {
          logger.debug("{} found in classpath", fileName);
        }
        return in;
      }

    } catch (FileNotFoundException ex) {
      // already made the check so this
      // should never be thrown
      throw new RuntimeException(ex);
    }
  }

  private static InputStream findInClassPath(String fileName) {
    return Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
  }

}
