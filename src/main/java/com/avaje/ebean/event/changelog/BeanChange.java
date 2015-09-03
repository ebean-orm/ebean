package com.avaje.ebean.event.changelog;

import com.avaje.ebean.ValuePair;

import java.util.Map;

/**
 * A bean insert, update or delete change sent as part of a ChangeSet.
 */
public class BeanChange {

  /**
   * The underling base table name.
   */
  String table;

  /**
   * The id value.
   */
  Object id;

  /**
   * The INSERT, UPDATE or DELETE change type.
   */
  ChangeType type;

  /**
   * The time the bean change was created.
   */
  long eventTime;

  /**
   * The values for insert or update. Note that null values are not included for insert.
   */
  Map<String, ValuePair> values;

  /**
   * Construct with all the details.
   */
  public BeanChange(String table, Object id, ChangeType type, Map<String, ValuePair> values) {
    this.table = table;
    this.id = id;
    this.type = type;
    this.eventTime = System.currentTimeMillis();
    this.values = values;
  }

  /**
   * Default constructor for JSON tools.
   */
  public BeanChange() {
  }

  public String toString() {
    return "table:" + table + " id:" + id+" values:"+values;
  }

  /**
   * Return the object type (typically table name).
   */
  public String getTable() {
    return table;
  }

  /**
   * Set the object type (for JSON tools).
   */
  public void setTable(String table) {
    this.table = table;
  }

  /**
   * Return the object id.
   */
  public Object getId() {
    return id;
  }

  /**
   * Set the bean id (for JSON tools).
   */
  public void setId(Object id) {
    this.id = this.id;
  }

  /**
   * Return the change type (INSERT, UPDATE or DELETE).
   */
  public ChangeType getType() {
    return type;
  }

  /**
   * Set the type (for JSON tools).
   */
  public void setType(ChangeType type) {
    this.type = type;
  }

  /**
   * Return the event time in epoch millis.
   */
  public long getEventTime() {
    return eventTime;
  }

  /**
   * Set the event time in epoch millis.
   */
  public void setEventTime(long eventTime) {
    this.eventTime = eventTime;
  }

  /**
   * Return the value pairs. For inserts the ValuePair oldValue is always null.
   */
  public Map<String, ValuePair> getValues() {
    return values;
  }

  /**
   * Set the value pairs (for JSON tools).
   */
  public void setValues(Map<String, ValuePair> values) {
    this.values = values;
  }
}
