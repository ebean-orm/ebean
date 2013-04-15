package com.avaje.ebean;

/**
 * Used to specify the type of like matching used.
 */
public enum LikeType {

  /**
   * You need to put in your own wildcards.
   */
  RAW,

  /**
   * The % wildcard is added to the end of the search word.
   */
  STARTS_WITH,

  /**
   * The % wildcard is added to the beginning of the search word.
   */
  ENDS_WITH,

  /**
   * The % wildcard is added to the beginning and end of the search word.
   */
  CONTAINS,

  /**
   * Uses equal to rather than a LIKE with wildcards.
   * <p>
   * This is mainly here to be available for use with ExampleExpression.
   * </p>
   */
  EQUAL_TO
}