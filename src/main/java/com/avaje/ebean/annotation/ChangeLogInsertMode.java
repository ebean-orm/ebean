package com.avaje.ebean.annotation;

/**
 * The mode used to determine if inserts should be included or not for a given bean type.
 */
public enum ChangeLogInsertMode {

  /**
   * Use the default behaviour as defined on ServerConfig.
   */
  DEFAULT,

  /**
   * Include inserts in the change log.
   */
  INCLUDE,

  /**
   * Exclude inserts in the change log.
   */
  EXCLUDE
}
