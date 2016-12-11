package io.ebeaninternal.server.expression;

/**
 * Simple operators - equals, greater than, less than etc.
 */
public enum Op {

  /**
   * Exists (JSON).
   */
  EXISTS(" is not null ", ""),

  /**
   * Not Exists (JSON).
   */
  NOT_EXISTS(" is null ", ""),

  /**
   * Between (JSON).
   */
  BETWEEN(" between ? and ? ", ""),

  /**
   * Equal to
   */
  EQ(" = ? ", ""),

  /**
   * Not equal to.
   */
  NOT_EQ(" <> ? ", ""),

  /**
   * Less than.
   */

  LT(" < ? ", "lt"),

  /**
   * Less than or equal to.
   */
  LT_EQ(" <= ? ", "lte"),

  /**
   * Greater than.
   */
  GT(" > ? ", "gt"),

  /**
   * Greater than or equal to.
   */
  GT_EQ(" >= ? ", "gte");

  final String exp;

  final String docExp;

  Op(String exp, String docExp) {
    this.exp = exp;
    this.docExp = docExp;
  }

  /**
   * Return the bind expression include JDBC ? bind placeholder.
   */
  public String bind() {
    return exp;
  }

  /**
   * Return the doc store expression.
   */
  public String docExp() {
    return docExp;
  }
}
