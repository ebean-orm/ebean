package io.ebeaninternal.server.expression.platform;

import io.ebeaninternal.server.expression.BitwiseOp;
import io.ebeaninternal.server.expression.Op;

/**
 * Oracle handling of platform specific expressions. ARRAY expressions not supported.
 */
final class OracleDbExpression extends BaseDbExpression {

  @Override
  public String concat(String property0, String separator, String property1, String suffix) {
    return concatOperator(property0, separator, property1, suffix);
  }

  @Override
  public void json(DbExpressionRequest request, String propName, String path, Op operator, Object value) {
    if (operator == Op.EXISTS) {
      request.append("json_exists(").property(propName).append(", '$.").append(path).append("')");
    } else if (operator == Op.NOT_EXISTS) {
      request.append("not json_exists(").property(propName).append(", '$.").append(path).append("')");
    } else {
      request.append("json_value(").property(propName).append(", '$.").append(path).append("')").append(operator.bind());
    }
  }

  @Override
  public void bitwise(DbExpressionRequest request, String propName, BitwiseOp operator, long flags, String compare, long match) {
    bitwiseFunction(request, propName, operator, compare);
  }

  @Override
  public void arrayContains(DbExpressionRequest request, String propName, boolean contains, Object... values) {
    throw new IllegalStateException("ARRAY expressions not supported on Oracle");
  }

  @Override
  public void arrayIsEmpty(DbExpressionRequest request, String propName, boolean empty) {
    throw new IllegalStateException("ARRAY expressions not supported on Oracle");
  }
}
