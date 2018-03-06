package io.ebean.config;

import java.util.Properties;

public class PropertiesWrapper {

  protected final Properties properties;

  protected final String prefix;

  protected final String serverName;

  /**
   * Construct with a prefix, serverName and properties.
   */
  public PropertiesWrapper(String prefix, String serverName, Properties properties) {
    this.serverName = serverName;
    this.prefix = prefix;
    this.properties = properties;
  }

  /**
   * Construct without prefix of serverName.
   */
  public PropertiesWrapper(Properties properties) {
    this(null, null, properties);
  }

  /**
   * Return the serverName (optional).
   */
  public String getServerName() {
    return serverName;
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
      value = internalGet(prefix + "." + serverName + "." + key);
    }
    if (value == null && prefix != null) {
      value = internalGet(prefix + "." + key);
    }
    if (value == null) {
      value = internalGet(key);
    }
    return value == null ? defaultValue : value;
  }

  private String internalGet(String key) {
    return properties.getProperty(key);
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
    String level = get(key, null);
    return (level == null) ? defaultValue : Enum.valueOf(enumType, level.toUpperCase());
  }

}
