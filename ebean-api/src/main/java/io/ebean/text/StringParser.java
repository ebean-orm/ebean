package io.ebean.text;

/**
 * Convert a String value into an Object value.
 * <p>
 * Basic interface to support CSV, JSON and XML processing.
 */
@FunctionalInterface
public interface StringParser {

  /**
   * Convert a String value into an Object value.
   */
  Object parse(String value);
}
