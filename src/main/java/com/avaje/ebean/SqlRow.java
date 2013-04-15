package com.avaje.ebean;

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
  public Iterator<String> keys();

  /**
   * Remove a property from the map. Returns the value of the removed property.
   */
  public Object remove(Object name);

  /**
   * Return a property value by its name.
   */
  public Object get(Object name);

  /**
   * Set a value to a property.
   */
  public Object put(String name, Object value);

  /**
   * Exactly the same as the put method.
   * <p>
   * I added this method because it seems more bean like to have get and set
   * methods.
   * </p>
   */
  public Object set(String name, Object value);

  /**
   * Return a property as a Boolean.
   */
  public Boolean getBoolean(String name);

  /**
   * Return a property as a UUID.
   */
  public UUID getUUID(String name);

  /**
   * Return a property as an Integer.
   */
  public Integer getInteger(String name);

  /**
   * Return a property value as a BigDecimal.
   */
  public BigDecimal getBigDecimal(String name);

  /**
   * Return a property value as a Long.
   */
  public Long getLong(String name);

  /**
   * Return the property value as a Double.
   */
  public Double getDouble(String name);

  /**
   * Return the property value as a Float.
   */
  public Float getFloat(String name);

  /**
   * Return a property as a String.
   */
  public String getString(String name);

  /**
   * Return the property as a java.util.Date.
   */
  public java.util.Date getUtilDate(String name);

  /**
   * Return the property as a sql date.
   */
  public Date getDate(String name);

  /**
   * Return the property as a sql timestamp.
   */
  public Timestamp getTimestamp(String name);

  /**
   * String description of the underlying map.
   */
  public String toString();

  /**
   * Clear the map.
   */
  public void clear();

  /**
   * Returns true if the map contains the property.
   */
  public boolean containsKey(Object key);

  /**
   * Returns true if the map contains the value.
   */
  public boolean containsValue(Object value);

  /**
   * Returns the entrySet of the map.
   */
  public Set<Map.Entry<String, Object>> entrySet();

  /**
   * Returns true if the map is empty.
   */
  public boolean isEmpty();

  /**
   * Returns the key set of the map.
   */
  public Set<String> keySet();

  /**
   * Put all the values from t into this map.
   */
  public void putAll(Map<? extends String, ? extends Object> t);

  /**
   * Return the size of the map.
   */
  public int size();

  /**
   * Return the values from this map.
   */
  public Collection<Object> values();

}