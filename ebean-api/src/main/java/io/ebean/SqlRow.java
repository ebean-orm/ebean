package io.ebean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Used to return raw SQL query results.
 * <p>
 * Refer to {@link SqlQuery} for examples.
 * </p>
 * <p>
 * There are convenience methods such as getInteger(), getBigDecimal() etc. The
 * reason for these methods is that the values put into this map often come
 * straight from the JDBC resultSet. Depending on the JDBC driver it may put a
 * different type into a given property. For example an Integer, BigDecimal,
 * Double could all be put into a property depending on the JDBC driver used.
 * These convenience methods automatically convert the value as required
 * returning the type you expect.
 * </p>
 */
public interface SqlRow extends Serializable, Map<String, Object> {

  /**
   * Return the property names (String).
   * <p>
   * Internally this uses LinkedHashMap and so the order of the property names
   * should be predictable and ordered by the use of LinkedHashMap.
   * </p>
   */
  Iterator<String> keys();

  /**
   * Remove a property from the map. Returns the value of the removed property.
   */
  @Override
  Object remove(Object name);

  /**
   * Return a property value by its name.
   */
  @Override
  Object get(Object name);

  /**
   * Set a value to a property.
   */
  @Override
  Object put(String name, Object value);

  /**
   * Exactly the same as the put method.
   * <p>
   * I added this method because it seems more bean like to have get and set
   * methods.
   * </p>
   */
  Object set(String name, Object value);

  /**
   * Return a property as a Boolean.
   */
  Boolean getBoolean(String name);

  /**
   * Return a property as a UUID.
   */
  UUID getUUID(String name);

  /**
   * Return a property as an Integer.
   */
  Integer getInteger(String name);

  /**
   * Return a property value as a BigDecimal.
   */
  BigDecimal getBigDecimal(String name);

  /**
   * Return a property value as a Long.
   */
  Long getLong(String name);

  /**
   * Return the property value as a Double.
   */
  Double getDouble(String name);

  /**
   * Return the property value as a Float.
   */
  Float getFloat(String name);

  /**
   * Return a property as a String.
   */
  String getString(String name);

  /**
   * Return the property as a java.util.Date.
   */
  java.util.Date getUtilDate(String name);

  /**
   * Return the property as a sql date.
   */
  Date getDate(String name);

  /**
   * Return the property as a sql timestamp.
   */
  Timestamp getTimestamp(String name);

  /**
   * String description of the underlying map.
   */
  @Override
  String toString();

  /**
   * Clear the map.
   */
  @Override
  void clear();

  /**
   * Returns true if the map contains the property.
   */
  @Override
  boolean containsKey(Object key);

  /**
   * Returns true if the map contains the value.
   */
  @Override
  boolean containsValue(Object value);

  /**
   * Returns the entrySet of the map.
   */
  @Override
  Set<Map.Entry<String, Object>> entrySet();

  /**
   * Returns true if the map is empty.
   */
  @Override
  boolean isEmpty();

  /**
   * Returns the key set of the map.
   */
  @Override
  Set<String> keySet();

  /**
   * Put all the values from t into this map.
   */
  @Override
  void putAll(Map<? extends String, ?> t);

  /**
   * Return the size of the map.
   */
  @Override
  int size();

  /**
   * Return the values from this map.
   */
  @Override
  Collection<Object> values();

}
