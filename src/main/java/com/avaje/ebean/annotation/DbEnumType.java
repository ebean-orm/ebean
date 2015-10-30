package com.avaje.ebean.annotation;

/**
 * Specify the DB storage type used to with <code>@DbEnumValue</code>.
 */
public enum DbEnumType {

  /**
   * Store values as database INTEGER.
   */
  INTEGER,

  /**
   * Store values as database VARCHAR.
   */
  VARCHAR
}
