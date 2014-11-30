package com.avaje.ebean.config;

import java.util.Properties;

public class PropertiesWrapper {

  protected final Properties properties;

  protected final String prefix;

  protected final String serverName;

  protected final PropertyMap propertyMap;

  /**
   * Construct with a prefix, serverName and properties.
   */
  public PropertiesWrapper(String prefix, String serverName, Properties properties) {
    this.serverName = serverName;
    this.prefix = prefix;
    this.propertyMap = PropertyMapLoader.load(null, properties);
    this.properties = propertyMap.asProperties();
  }

  /**
   * Construct without prefix of serverName.
   */
  public PropertiesWrapper(Properties properties) {
    this(null, null, properties);
  }

  /**
   * Internal copy constructor when changing prefix.
   */
  protected PropertiesWrapper(String prefix, String serverName, PropertyMap propertyMap, Properties properties) {
    this.serverName = serverName;
    this.prefix = prefix;
    this.propertyMap = propertyMap;
    this.properties = properties;
  }

  /**
   * Return a PropertiesWrapper instance with a different prefix but same underlying properties.
   * <p/>
   * Used when wanting to use "datasource" as the prefix rather than "ebean".
   * <p/>
   * The returning instance should only be used in a read only fashion.
   */
  public PropertiesWrapper withPrefix(String prefix) {
    return new PropertiesWrapper(prefix, serverName, propertyMap, properties);
  }

  /**
   * Return the serverName (optional).
   */
  public String getServerName() {
    return serverName;
  }

  /**
   * Return as Properties with lower case keys and after evaluation and additional properties loading has occurred.
   * <p>
   * Ebean has historically ignored the case of keys hence returning the Properties with all the keys lower cased.
   * </p>
   */
  public Properties asPropertiesLowerCase() {
    return properties;
  }

  /**
   * Get a property with no default value.
   */
  public String get(String key) {
    return get(key, null);
  }

  /**
   * Get a property with a default value.
   * <p>
   * This performs a search using the prefix and server name (if supplied) to search for the property
   * value in order based on:
   * <pre>{@code
   *   prefix.serverName.key
   *   prefix.key
   *   key
   * }</pre>
   * </p>
   */
  public String get(String key, String defaultValue) {

    String value = null;
    if (serverName != null && prefix != null) {
      value = propertyMap.get(prefix + "." + serverName + "." + key, null);
    }
    if (value == null && prefix != null) {
      value = propertyMap.get(prefix + "." + key, null);
    }
    if (value == null) {
      value = propertyMap.get(key, null);
    }
    return value == null ? defaultValue : value;
  }

  /**
   * Return a double property value.
   */
  public double getDouble(String key, double defaultValue) {

    String value = get(key, String.valueOf(defaultValue));
    return Double.parseDouble(value);
  }

  /**
   * Return an int property value.
   */
  public int getInt(String key, int defaultValue) {

    String value = get(key, String.valueOf(defaultValue));
    return Integer.parseInt(value);
  }

  /**
   * Return a long property value.
   */
  public long getLong(String key, long defaultValue) {

    String value = get(key, String.valueOf(defaultValue));
    return Long.parseLong(value);
  }

  /**
   * Return a boolean property value.
   */
  public boolean getBoolean(String key, boolean defaultValue) {

    String value = get(key, String.valueOf(defaultValue));
    return Boolean.parseBoolean(value);
  }

  /**
   * Return a Enum property value.
   */
  public <T extends Enum<T>> T getEnum(Class<T> enumType, String key, T defaultValue) {
    String level = get(key, defaultValue.name());
    return Enum.valueOf(enumType, level.toUpperCase());
  }

}
