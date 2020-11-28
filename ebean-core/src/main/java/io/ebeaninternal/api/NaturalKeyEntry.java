package io.ebeaninternal.api;

/**
 * An entry for natural key lookup.
 */
public interface NaturalKeyEntry {

  /**
   * Return the natural cache key (String concatenation of values).
   */
  String key();

  /**
   * Return the inValue (used to remove from IN clause of original query).
   */
  Object getInValue();
}
