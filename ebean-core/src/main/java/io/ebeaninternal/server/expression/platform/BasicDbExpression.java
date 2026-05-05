package io.ebeaninternal.server.expression.platform;

import io.ebeaninternal.server.expression.Op;

/**
 * Not supported JSON or ARRAY expression handler.
 */
class BasicDbExpression extends BaseDbExpression {

  @Override
  public void json(DbExpressionRequest request, String propName, String path, Op operator, Object value) {
    throw new RuntimeException("JSON expressions only supported on Postgres and Oracle");
  }

  @Override
  public void arrayContains(DbExpressionRequest request, String propName, boolean contains, Object... values) {
    throw new RuntimeException("ARRAY expressions only supported on Postgres");
  }

  @Override
  public void arrayIsEmpty(DbExpressionRequest request, String propName, boolean empty) {
    throw new RuntimeException("ARRAY expressions only supported on Postgres");
  }
}
