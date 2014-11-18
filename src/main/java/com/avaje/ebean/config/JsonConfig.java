package com.avaje.ebean.config;

/**
 * Configuration for JSON features.
 */
public abstract class JsonConfig {

  /**
   * Defined the format used for DateTime types.
   */
  public enum DateTime {

    /**
     * Format as epoch millis.
     */
    MILLIS,

    /**
     * Format as epoch with nanos.
     */
    NANOS,

    /**
     * Format as ISO-8601 date format.
     */
    ISO8601
  }

}
