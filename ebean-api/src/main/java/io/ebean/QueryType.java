package io.ebean;

/**
 * The type of the query being executed.
 */
public enum QueryType {

  /**
   * A find query.
   */
  FIND,

  /**
   * An update query.
   */
  UPDATE,

  /**
   * A delete query.
   */
  DELETE
}
