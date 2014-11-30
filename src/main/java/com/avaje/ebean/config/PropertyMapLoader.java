package com.avaje.ebean.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Map;
import java.util.Properties;

/**
 * Helper used to load the ebean.properties into a PropertyMap.
 */
final class PropertyMapLoader {

  private static final Logger logger = LoggerFactory.getLogger(PropertyMapLoader.class);

  public static PropertyMap loadGlobalProperties() {

    String fileName = System.getenv("EBEAN_PROPS_FILE");
    if (fileName == null) {
      fileName = System.getProperty("ebean.props.file");
      if (fileName == null) {
        fileName = "ebean.properties";
      }
    }

    return load(null, fileName);
  }

  /**
   * Load the file returning the property map.
   * 
   * @param p
   *          an existing property map to load into.
   * @param fileName
   *          the name of the properties file to load.
   */
  public static PropertyMap load(PropertyMap p, String fileName) {

    InputStream is = findInputStream(fileName);
    if (is == null) {
      logger.error(fileName + " not found");
      return p;
    } else {
      return load(p, is);
    }
  }

  /**
   * Load the inputstream returning the property map.
   * 
   * @param p
   *          an existing property map to load into.
   * @param in
   *          the InputStream of the properties file to load.
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
      otherProps = otherProps.replace("\\", "/");
      InputStream is = findInputStream(otherProps);
      if (is != null) {
        logger.debug("loading properties from " + otherProps);
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
        logger.debug(fileName + " found in file system");
        return new FileInputStream(f);
      } else {
        InputStream in = findInClassPath(fileName);
        if (in != null) {
          logger.debug(fileName + " found in classpath");
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
