package io.ebeaninternal.server.expression.platform;

import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.server.expression.Op;

/**
 * MariaDB specific handling of platform specific expressions.
 */
class MariaDbExpression extends BasicDbExpression {

  @Override
  public void json(SpiExpressionRequest request, String propName, String path, Op operator, Object value) {
    request.append("(").append(propName).append(" ->> '$.").append(path).append("')");
    request.append(operator.bind());
  }
}
