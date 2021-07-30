package io.ebean.bean;

/**
 * Represents a next value to use for mutable content properties (DbJson with jackson beans).
 */
public interface MutableValueNext {

  /**
   * Return the next content to use. Provided such that we serialise to json once.
   */
  String content();

  /**
   * Return the next MutableValueInfo to use after an update.
   */
  MutableValueInfo info();
}
