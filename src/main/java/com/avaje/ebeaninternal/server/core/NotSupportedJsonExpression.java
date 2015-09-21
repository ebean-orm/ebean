package com.avaje.ebeaninternal.server.core;

import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.server.expression.Op;

/**
 * Not supported JSON expression handler.
 */
public class NotSupportedJsonExpression implements JsonExpressionHandler {

  @Override
  public void addSql(SpiExpressionRequest request, String propName, String path, Op operator, Object value) {
    throw new RuntimeException("JSON expressions only supported on Postgres and Oracle");
  }
}
