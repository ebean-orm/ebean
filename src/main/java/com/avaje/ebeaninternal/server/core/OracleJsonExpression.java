package com.avaje.ebeaninternal.server.core;

import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.server.expression.Op;

/**
 * Postgres JSON expression handler
 */
public class OracleJsonExpression implements JsonExpressionHandler {

  @Override
  public void addSql(SpiExpressionRequest request, String propName, String path, Op operator, Object value) {

    if (operator == Op.EXISTS) {
      request.append("json_exists(").append(propName).append(", '$.").append(path).append("')");
    } else if (operator == Op.NOT_EXISTS) {
      request.append("not json_exists(").append(propName).append(", '$.").append(path).append("')");
    } else {
      request.append("json_value(").append(propName).append(", '$.").append(path).append("')");
      request.append(operator.bind());
    }

  }
}
