package io.ebeaninternal.server.type;

import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * Used to map Bean values to DB values.
 * <p>
 * Useful for building Enum converters where you want to map the DB values an
 * Enum gets converter to.
 * </p>
 */
abstract class EnumToDbValueMap<T> {

  static EnumToDbValueMap<?> create(boolean integerType) {
    return integerType ? new EnumToDbIntegerMap() : new EnumToDbStringMap();
  }

  final LinkedHashMap<Object, T> keyMap;
  final LinkedHashMap<T, Object> valueMap;
  final HashMap<Object, Object> nameMap;
  final boolean allowNulls;
  final boolean isIntegerType;

  /**
   * Construct with allowNulls defaulting to false.
   */
  EnumToDbValueMap() {
    this(false, false);
  }

  /**
   * Construct with allowNulls setting.
   * <p>
   * If allowNulls is false then an IllegalArgumentException is thrown by
   * either the getDBValue or getBeanValue methods if not matching Bean or DB
   * value is found.
   * </p>
   */
  EnumToDbValueMap(boolean allowNulls, boolean isIntegerType) {
    this.allowNulls = allowNulls;
    this.isIntegerType = isIntegerType;
    this.keyMap = new LinkedHashMap<>();
    this.valueMap = new LinkedHashMap<>();
    this.nameMap = new HashMap<>();
  }

  /**
   * Return true if this is mapping to integers, false
   * if mapping to Strings.
   */
  boolean isIntegerType() {
    return isIntegerType;
  }

  /**
   * Return the DB values.
   */
  public Iterator<T> dbValues() {
    return valueMap.keySet().iterator();
  }

  /**
   * Bind using the correct database type.
   */
  public abstract void bind(DataBinder binder, Object value) throws SQLException;

  /**
   * Read using the correct database type.
   */
  public abstract Object read(DataReader reader) throws SQLException;

  /**
   * Return the database type.
   */
  public abstract int getDbType();

  /**
   * Add name value pair where the dbValue is the raw string and may need to
   * be converted (to an Integer for example).
   */
  public abstract EnumToDbValueMap<T> add(Object beanValue, String dbValue, String name);

  /**
   * Add a bean value and DB value pair.
   * <p>
   * The dbValue will be converted to an Integer if isIntegerType is true;
   * </p>
   */
  protected void addInternal(Object beanValue, T dbValue, String name) {
    keyMap.put(beanValue, dbValue);
    valueMap.put(dbValue, beanValue);
    nameMap.put(name, beanValue);
  }

  /**
   * Return the DB value given the bean value.
   */
  public T getDbValue(Object beanValue) {
    if (beanValue == null) {
      return null;
    }
    T dbValue = keyMap.get(beanValue);
    if (dbValue == null && !allowNulls) {
      String msg = "DB value for " + beanValue + " not found in " + keyMap;
      throw new IllegalArgumentException(msg);
    }
    return dbValue;
  }

  /**
   * Return the Bean value given the DB value.
   */
  public Object getBeanValue(T dbValue) {
    if (dbValue == null) {
      return null;
    }
    Object beanValue = valueMap.get(dbValue);
    if (beanValue == null) {
      beanValue = nameMap.get(dbValue);
    }
    if (beanValue == null && !allowNulls) {
      String msg = "Bean value for " + dbValue + " not found in " + valueMap;
      throw new IllegalArgumentException(msg);
    }
    return beanValue;
  }
}
