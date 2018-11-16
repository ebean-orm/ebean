package io.ebeaninternal.server.expression.platform;

import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.server.expression.BitwiseOp;

/**
 * H2 handling of platform specific expressions.
 */
class H2DbExpression extends BasicDbExpression {

  @Override
  public void bitwise(SpiExpressionRequest request, String propName, BitwiseOp operator, long flags, String compare, long match) {

    bitwiseFunction(request, propName, operator, compare);
  }
}
