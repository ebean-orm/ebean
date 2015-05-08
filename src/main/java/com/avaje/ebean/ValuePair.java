package com.avaje.ebean;

/**
 * Holds two values as the result of a difference comparison.
 */
public class ValuePair {

  private final Object newValue;

  private final Object oldValue;

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
   * Return the new value.
   */
  @Deprecated
  public Object getValue1() {
    return newValue;
  }

  /**
   * Return the old value.
   */
  @Deprecated
  public Object getValue2() {
    return oldValue;
  }

  public String toString() {
    return newValue + "," + oldValue;
  }
}
