package io.ebeaninternal.server.expression.platform;

import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.server.expression.BitwiseOp;
import io.ebeaninternal.server.expression.Op;

/**
 * HANA handling of platform specific expressions.
 */
public class HanaDbExpression extends BaseDbExpression {

  @Override
  public void bitwise(SpiExpressionRequest request, String propName, BitwiseOp operator, long flags, String compare, long match) {
    bitwiseFunction(request, propName, operator, compare);
  }

  @Override
  public void json(SpiExpressionRequest request, String propName, String path, Op operator, Object value) {
    request.append("json_value(").append(propName).append(", '$.").append(path).append("')");
    request.append(operator.bind());
  }

  @Override
  public void arrayIsEmpty(SpiExpressionRequest request, String propName, boolean empty) {
    request.append("cardinality(").append(propName).append(")");
    if (empty) {
      request.append(" = 0");
    } else {
      request.append(" <> 0");
    }
  }

  @Override
  public String concat(String property0, String separator, String property1, String suffix) {
    StringBuilder sb = new StringBuilder();
    sb.append("concat(").append(property0).append(", '").append(separator).append("'||").append(property1);
    if (suffix != null && !suffix.isEmpty()) {
      sb.append("||'").append(suffix).append('\'');
    }
    sb.append(")");
    return sb.toString();
  }

  @Override
  public void arrayContains(SpiExpressionRequest request, String propName, boolean contains, Object... values) {
    for (int i = 0; i < values.length; i++) {
      if (i > 0) {
        request.append(" and ");
      }
      request.append("(?");
      if (!contains) {
        request.append(" not ");
      }
      request.append(" member of ").append(propName).append(")");

    }
  }

}
