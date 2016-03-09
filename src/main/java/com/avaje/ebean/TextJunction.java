package com.avaje.ebean;

/**
 * A Full text MUST, MUST NOT or SHOULD group of expressions.
 */
public interface TextJunction<T> extends Junction<T>, TextExpressionList<T> {

  /**
   * The type of Junction used in full text expressions.
   */
  enum Type {

    /**
     * Logically a AND group.
     */
    MUST("must"),

    /**
     * Logically a NOT group.
     */
    MUST_NOT("must_not"),

    /**
     * Logically a OR group.
     */
    SHOULD("should");

    String literal;

    Type(String literal) {
      this.literal = literal;
    }

    /**
     * Return the literal value for this type.
     */
    public String literal() {
      return literal;
    }
  }
}
