package com.avaje.ebeaninternal.server.expression;

/**
 * Simple operators - equals, greater than, less than etc.
 */
public enum Op {

  /**
   * Exists (JSON).
   */
  EXISTS(" is not null "),

  /**
   * Not Exists (JSON).
   */
  NOT_EXISTS(" is null "),

  /**
   * Between (JSON).
   */
  BETWEEN(" between ? and ? "),

  /**
   * Equal to
   */
  EQ(" = ? "),

  /**
   * Not equal to.
   */
  NOT_EQ(" <> ? "),

  /**
   *
   * Less than.
   */

  LT(" < ? "),

  /**
   * Less than or equal to.
   */
  LT_EQ(" <= ? "),

  /**
   * Greater than.
   */
  GT(" > ? "),

  /**
   * Greater than or equal to.
   */
  GT_EQ(" >= ? ");

  final String exp;

  Op(String exp) {
    this.exp = exp;
  }

  /**
   * Return the bind expression include JDBC ? bind placeholder.
   */
  public String bind() {
    return exp;
  }
}
