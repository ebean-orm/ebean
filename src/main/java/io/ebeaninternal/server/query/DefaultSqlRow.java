package io.ebeaninternal.server.query;

import io.ebean.SqlQuery;
import io.ebean.SqlRow;
import io.ebeaninternal.server.core.BasicTypeConverter;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
public class DefaultSqlRow implements SqlRow {

  private static final long serialVersionUID = -3120927797041336242L;

  private final String dbTrueValue;
  private final boolean optimizedBinaryUUID;

  /**
   * The underlying map of property data.
   */
  private final Map<String, Object> map;

  /**
   * Create with an initialCapacity and loadFactor.
   * <p>
   * The defaults of these are 16 and 0.75.
   * </p>
   * <p>
   * Note that the Map will rehash the contents when the number of keys in
   * this map reaches its threshold (initialCapacity * loadFactor).
   * </p>
   */
  public DefaultSqlRow(int initialCapacity, float loadFactor, String dbTrueValue, boolean optimizedBinaryUUID) {
    this.map = new LinkedHashMap<>(initialCapacity, loadFactor);
    this.dbTrueValue = dbTrueValue;
    this.optimizedBinaryUUID = optimizedBinaryUUID;
  }

  @Override
  public Iterator<String> keys() {
    return map.keySet().iterator();
  }

  /**
   * Keys internally always lower cased to take out differences in database dictionaries.
   */
  private Object asKey(Object name) {
    return ((String) name).toLowerCase();
  }

  @Override
  public Object remove(Object name) {
    return map.remove(asKey(name));
  }

  @Override
  public Object get(Object name) {
    return map.get(asKey(name));
  }

  @Override
  public Object put(String name, Object value) {
    return setInternal(name, value);
  }

  @Override
  public Object set(String name, Object value) {
    return setInternal(name, value);
  }

  private Object setInternal(String name, Object newValue) {
    return map.put(name.toLowerCase(), newValue);
  }

  @Override
  public UUID getUUID(String name) {
    return BasicTypeConverter.toUUID(get(name), optimizedBinaryUUID);
  }

  @Override
  public Boolean getBoolean(String name) {
    return BasicTypeConverter.toBoolean(get(name), dbTrueValue);
  }

  @Override
  public Integer getInteger(String name) {
    return BasicTypeConverter.toInteger(get(name));
  }

  @Override
  public BigDecimal getBigDecimal(String name) {
    return BasicTypeConverter.toBigDecimal(get(name));
  }

  @Override
  public Long getLong(String name) {
    return BasicTypeConverter.toLong(get(name));
  }

  @Override
  public Double getDouble(String name) {
    return BasicTypeConverter.toDouble(get(name));
  }

  @Override
  public Float getFloat(String name) {
    return BasicTypeConverter.toFloat(get(name));
  }

  @Override
  public String getString(String name) {
    return BasicTypeConverter.toString(get(name));
  }

  @Override
  public java.util.Date getUtilDate(String name) {
    return BasicTypeConverter.toUtilDate(get(name));
  }

  @Override
  public Date getDate(String name) {
    return BasicTypeConverter.toDate(get(name));
  }

  @Override
  public Timestamp getTimestamp(String name) {
    return BasicTypeConverter.toTimestamp(get(name));
  }

  @Override
  public String toString() {
    return map.toString();
  }

  // ------------------------------------
  // Normal map methods...

  @Override
  public void clear() {
    map.clear();
  }

  @Override
  public boolean containsKey(Object key) {
    return map.containsKey(asKey(key));
  }

  @Override
  public boolean containsValue(Object value) {
    return map.containsValue(value);
  }

  @Override
  public Set<Map.Entry<String, Object>> entrySet() {
    return map.entrySet();
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  @Override
  public Set<String> keySet() {
    return map.keySet();
  }

  @Override
  public void putAll(Map<? extends String, ?> t) {
    map.putAll(t);
  }

  @Override
  public int size() {
    return map.size();
  }

  @Override
  public Collection<Object> values() {
    return map.values();
  }

}
