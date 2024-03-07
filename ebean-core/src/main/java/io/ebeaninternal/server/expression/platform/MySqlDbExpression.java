package io.ebeaninternal.server.expression.platform;

import io.ebeaninternal.server.expression.Op;

/**
 * MySql specific handling of platform specific expressions.
 */
final class MySqlDbExpression extends BasicDbExpression {

  @Override
  public void json(DbExpressionRequest request, String propName, String path, Op operator, Object value) {
    request.append('(').property(propName).append(" ->> '$.").append(path).append("')").append(operator.bind());
  }

}
