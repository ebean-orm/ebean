package com.avaje.ebean;

/**
 * Holds two values as the result of a difference comparison.
 */
public class ValuePair {

  protected Object newValue;

  protected Object oldValue;

  /**
   * Default constructor for JSON tools.
   */
  public ValuePair() {
  }

  /**
   * Construct with the pair of new and old values.
   */
  public ValuePair(Object newValue, Object oldValue) {
    this.newValue = newValue;
    this.oldValue = oldValue;
  }

  /**
   * Return the new value.
   */
  public Object getNewValue() {
    return newValue;
  }
  
  /**
   * Return the old value.
   */
  public Object getOldValue() {
    return oldValue;
  }

  /**
   * Set the new value.
   */
  public void setNewValue(Object newValue) {
    this.newValue = newValue;
  }

  /**
   * Set the old value.
   */
  public void setOldValue(Object oldValue) {
    this.oldValue = oldValue;
  }

  public String toString() {
    return newValue + "," + oldValue;
  }
}
