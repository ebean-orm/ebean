package io.ebeaninternal.server.expression.platform;

/**
 * Request building the expression sql.
 */
public interface DbExpressionRequest {

  /**
   * Append to the expression sql without any parsing.
   */
  DbExpressionRequest append(String expression);

  /**
   * Append to the expression sql without any parsing.
   */
  DbExpressionRequest append(char c);

  /**
   * Append to the expression sql with logical property parsing to db columns with logical path prefix.
   */
  DbExpressionRequest property(String expression);
}
