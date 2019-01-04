package io.ebeaninternal.server.expression.platform;

import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.server.expression.BitwiseOp;

/**
 * Not supported JSON or ARRAY expression handler.
 */
abstract class BaseDbExpression implements DbExpressionHandler {

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

  @Override
  public String concat(String property0, String separator, String property1, String suffix) {
    StringBuilder sb = new StringBuilder();
    sb.append("concat(").append(property0).append(",'").append(separator).append("',").append(property1);
    if (suffix != null && !suffix.isEmpty()) {
      sb.append(",'").append(suffix).append('\'');
    }
    sb.append(')');
    return sb.toString();
  }
}
