package io.ebeaninternal.server.expression.platform;

import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.server.expression.Op;

/**
 * Microsoft SQL Server JSON. ARRAY expressions not supported.
 */
public class SqlServerDbExpression extends BaseDbExpression {

  @Override
  public void json(final SpiExpressionRequest request, final String propName,
                   final String path, final Op operator, final Object value) {
    request.append("json_value(").append(propName).append(", '$.").append(path).append("')");
    request.append(operator.bind());
  }

  @Override
  public void arrayContains(final SpiExpressionRequest request, final String propName,
                            final boolean contains, final Object... values) {
    throw new RuntimeException("ARRAY expressions not supported on Microsoft SQL Server");
  }

  @Override
  public void arrayIsEmpty(final SpiExpressionRequest request, final String propName, final boolean empty) {
    throw new RuntimeException("ARRAY expressions not supported on Microsoft SQL Server");
  }
}
