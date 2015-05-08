package com.avaje.ebeaninternal.server.query;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import com.avaje.ebeaninternal.server.core.BasicTypeConverter;

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

    static final long serialVersionUID = -3120927797041336242L;

    private final String dbTrueValue;
    
    /**
     * The underlying map of property data.
     */
    Map<String, Object> map;

    /**
     * Create with a specific Map implementation.
     * <p>
     * The default Map implementation is LinkedHashMap.
     * </p>
     */
    public DefaultSqlRow(Map<String, Object> map, String dbTrueValue) {
        this.map = map;
        this.dbTrueValue = dbTrueValue;
    }

    /**
     * Create a new MapBean based on a LinkedHashMap with default
     * initialCapacity (of 16).
     */
    public DefaultSqlRow(String dbTrueValue) {
        this.map = new LinkedHashMap<String, Object>();
        this.dbTrueValue = dbTrueValue;
    }

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
    public DefaultSqlRow(int initialCapacity, float loadFactor, String dbTrueValue) {
        this.map = new LinkedHashMap<String, Object>(initialCapacity, loadFactor);
        this.dbTrueValue = dbTrueValue;
    }

    public Iterator<String> keys() {
        return map.keySet().iterator();
    }

    public Object remove(Object name) {
        name = ((String) name).toLowerCase();
        return map.remove(name);
    }

    public Object get(Object name) {
        name = ((String) name).toLowerCase();
        return map.get(name);
    }

    public Object put(String name, Object value) {
        return setInternal(name, value);
    }

    public Object set(String name, Object value) {
        return setInternal(name, value);
    }

    private Object setInternal(String name, Object newValue) {
        // MapBean properties are always lowercase
        name = name.toLowerCase();

        // valueList = null;
        return map.put(name, newValue);
    }

    public UUID getUUID(String name) {
        Object val = get(name);
        return BasicTypeConverter.toUUID(val);
    }

    public Boolean getBoolean(String name) {
        Object val = get(name);
        return BasicTypeConverter.toBoolean(val, dbTrueValue);
    }

    public Integer getInteger(String name) {
        Object val = get(name);
        return BasicTypeConverter.toInteger(val);
    }

    public BigDecimal getBigDecimal(String name) {
        Object val = get(name);
        return BasicTypeConverter.toBigDecimal(val);
    }

    public Long getLong(String name) {
        Object val = get(name);
        return BasicTypeConverter.toLong(val);
    }

    public Double getDouble(String name) {
        Object val = get(name);
        return BasicTypeConverter.toDouble(val);
    }

    public Float getFloat(String name) {
        Object val = get(name);
        return BasicTypeConverter.toFloat(val);
    }

    public String getString(String name) {
        Object val = get(name);
        return BasicTypeConverter.toString(val);
    }

    public java.util.Date getUtilDate(String name) {
        Object val = get(name);
        return BasicTypeConverter.toUtilDate(val);
    }

    public Date getDate(String name) {
        Object val = get(name);
        return BasicTypeConverter.toDate(val);
    }

    public Timestamp getTimestamp(String name) {
        Object val = get(name);
        return BasicTypeConverter.toTimestamp(val);
    }

    public String toString() {
        return map.toString();
    }

    // ------------------------------------
    // Normal map methods...

    public void clear() {
        map.clear();
    }

    public boolean containsKey(Object key) {
        key = ((String) key).toLowerCase();
        return map.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    public Set<Map.Entry<String, Object>> entrySet() {
        return map.entrySet();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    public void putAll(Map<? extends String, ? extends Object> t) {
        map.putAll(t);
    }

    public int size() {
        return map.size();
    }

    public Collection<Object> values() {
        return map.values();
    }

}
