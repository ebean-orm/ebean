package io.ebeaninternal.api;

/**
 * A property value pair in a natural key lookup.
 */
public class NaturalKeyEq {

  final String property;
  final Object value;

  public NaturalKeyEq(String property, Object value) {
    this.property = property;
    this.value = value;
  }
}
