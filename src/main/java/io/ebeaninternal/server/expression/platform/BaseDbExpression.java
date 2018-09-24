package io.ebeaninternal.server.expression.platform;

import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.server.expression.BitwiseOp;

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

  @Override
  public void bitwise(SpiExpressionRequest request, String propName, BitwiseOp operator, long flags, String compare, long match) {

    String bitOp = bitOp(operator);
    request.append("(").append(propName).append(" ").append(bitOp).append(" ? ").append(compare).append(" ?)");
  }

  private String bitOp(BitwiseOp operator) {
    switch (operator) {
      case ANY:
      case AND:
      case ALL:
        return "&";
      default:
        throw new IllegalArgumentException("Unknown Bitwise operator " + operator + " not handled?");
    }
  }

  /**
   * Common alternative where the bitwise operation is a function (specifically bitand is used - H2 and Oracle).
   */
  protected void bitwiseFunction(SpiExpressionRequest request, String propName, BitwiseOp operator, String compare) {

    String funcName = functionName(operator);
    request.append(funcName).append("(").append(propName).append(", ?) ").append(compare).append(" ?");
  }

  protected String functionName(BitwiseOp operator) {
    switch (operator) {
      case AND:
      case ANY:
      case ALL:
        return "bitand";
      default:
        throw new IllegalArgumentException("Unknown Bitwise operator " + operator + " not handled?");
    }
  }

}
