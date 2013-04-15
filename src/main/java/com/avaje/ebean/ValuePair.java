package com.avaje.ebean;

/**
 * Holds two values as the result of a difference comparison.
 */
public class ValuePair {

  final Object value1;

  final Object value2;

  public ValuePair(Object value1, Object value2) {
    this.value1 = value1;
    this.value2 = value2;
  }

  /**
   * Return the first value.
   */
  public Object getValue1() {
    return value1;
  }

  /**
   * Return the second value.
   */
  public Object getValue2() {
    return value2;
  }

  public String toString() {
    return value1 + "," + value2;
  }
}
