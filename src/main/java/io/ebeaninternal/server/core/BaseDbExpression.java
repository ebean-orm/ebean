package io.ebeaninternal.server.core;

/**
 * Not supported JSON or ARRAY expression handler.
 */
abstract class BaseDbExpression implements DbExpressionHandler {

  private final String concatOperator;

  BaseDbExpression(String concatOperator) {
    this.concatOperator = concatOperator;
  }

  @Override
  public String getConcatOperator() {
    return concatOperator;
  }

}
