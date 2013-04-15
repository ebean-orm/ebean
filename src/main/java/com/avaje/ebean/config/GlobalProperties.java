package com.avaje.ebean.config;

import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletContext;

import com.avaje.ebean.util.ClassUtil;

/**
 * Provides access to properties loaded from the ebean.properties file.
 */
public final class GlobalProperties {

  private static volatile PropertyMap globalMap;

  private static boolean skipPrimaryServer;

  /**
   * Set whether to skip automatically creating the primary server.
   */
  public static synchronized void setSkipPrimaryServer(boolean skip) {
    skipPrimaryServer = skip;
  }

  /**
   * Return true to skip automatically creating the primary server.
   */
  public static synchronized boolean isSkipPrimaryServer() {
    return skipPrimaryServer;
  }

  /**
   * Parse the string replacing any expressions like ${catalina.base}.
   * <p>
   * This will evaluate expressions using first environment variables, than java
   * system variables and lastly properties in ebean.properties - in that order.
   * </p>
   * <p>
   * Expressions start with "${" and end with "}".
   * </p>
   */
  public static String evaluateExpressions(String val) {
    return getPropertyMap().eval(val);
  }

  /**
   * Parse and evaluate any expressions that have not already been evaluated.
   */
  public static synchronized void evaluateExpressions() {
    getPropertyMap().evaluateProperties();
  }

  /**
   * In a servlet container environment this will additionally look in WEB-INF
   * for the ebean.properties file.
   */
  public static synchronized void setServletContext(ServletContext servletContext) {

    PropertyMapLoader.setServletContext(servletContext);
  }

  /**
   * Return the ServletContext (if setup in a servlet container environment).
   */
  public static synchronized ServletContext getServletContext() {

    return PropertyMapLoader.getServletContext();
  }

  private static void initPropertyMap() {

    String fileName = System.getenv("EBEAN_PROPS_FILE");
    if (fileName == null) {
      fileName = System.getProperty("ebean.props.file");
      if (fileName == null) {
        fileName = "ebean.properties";
      }
    }

    globalMap = PropertyMapLoader.load(null, fileName);
    if (globalMap == null) {
      // ebean.properties file was not found... but that
      // is ok because we are likely doing programmatic config
      globalMap = new PropertyMap();
    }

    String loaderCn = globalMap.get("ebean.properties.loader");
    if (loaderCn != null) {
      // a Runnable that can be used to customise the initialisation
      // of the GlobalProperties
      try {
        Runnable r = (Runnable) ClassUtil.newInstance(loaderCn);
        r.run();
      } catch (Exception e) {
        String m = "Error creating or running properties loader " + loaderCn;
        throw new RuntimeException(m, e);
      }
    }
  }

  /**
   * Return the property map loading it if required.
   */
  private static synchronized PropertyMap getPropertyMap() {

    if (globalMap == null) {
      initPropertyMap();
    }

    return globalMap;
  }

  /**
   * Return a String property with a default value.
   */
  public static synchronized String get(String key, String defaultValue) {
    return getPropertyMap().get(key, defaultValue);
  }

  /**
   * Return a int property with a default value.
   */
  public static synchronized int getInt(String key, int defaultValue) {
    return getPropertyMap().getInt(key, defaultValue);
  }

  /**
   * Return a boolean property with a default value.
   */
  public static synchronized boolean getBoolean(String key, boolean defaultValue) {
    return getPropertyMap().getBoolean(key, defaultValue);
  }

  /**
   * Set a property return the previous value. This will evaluate any
   * expressions in the value.
   */
  public static synchronized String put(String key, String value) {
    return getPropertyMap().putEval(key, value);
  }

  /**
   * Set a Map of key value properties.
   */
  public static synchronized void putAll(Map<String, String> keyValueMap) {
    for (Entry<String, String> e : keyValueMap.entrySet()) {
      getPropertyMap().putEval(e.getKey(), e.getValue());
    }
  }

  public static PropertySource getPropertySource(String name) {
    return new ConfigPropertyMap(name);
  }

  public static interface PropertySource {

    /**
     * Return the name of the server. This is also the dataSource name.
     */
    public String getServerName();

    /**
     * Get a property. This will prepend "ebean" and the server name to lookup
     * the value.
     */
    public String get(String key, String defaultValue);

    public int getInt(String key, int defaultValue);

    public boolean getBoolean(String key, boolean defaultValue);

    public <T extends Enum<T>> T getEnum(Class<T> enumType, String key, T defaultValue);

  }

  public static class DelegatedGlobalPropertySource implements PropertySource {

    private String serverName;

    public DelegatedGlobalPropertySource(String serverName) {
      this.serverName = serverName;
    }

    @Override
    public String getServerName() {
      return serverName;
    }

    @Override
    public String get(String key, String defaultValue) {
      return GlobalProperties.get(key, defaultValue);
    }

    @Override
    public int getInt(String key, int defaultValue) {
      return GlobalProperties.getInt(key, defaultValue);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
      return GlobalProperties.getBoolean(key, defaultValue);
    }

    @Override
    public <T extends Enum<T>> T getEnum(Class<T> enumType, String key, T defaultValue) {
      String level = get(key, defaultValue.name());
      return Enum.valueOf(enumType, level.toUpperCase());
    }
  }
}
