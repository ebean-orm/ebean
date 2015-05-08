package com.avaje.ebean.text;

/**
 * Convert an Object value into a String value.
 * <p>
 * Basic interface to support CSV, JSON and XML processing.
 * </p>
 * 
 * @author rbygrave
 */
public interface StringFormatter {

  /**
   * Convert an Object value into a String value.
   */
  String format(Object value);
}
