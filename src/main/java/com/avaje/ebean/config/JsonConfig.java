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


  public enum Include {

    /**
     * Include all values including null and empty collections.
     */
    ALL,

    /**
     * Exclude null values (include empty collections).
     */
    NON_NULL,

    /**
     * Exclude null values and empty collections.
     */
    NON_EMPTY
  }
}
