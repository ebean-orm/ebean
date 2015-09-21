package com.avaje.ebeaninternal.server.core;

import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.server.expression.Op;

/**
 * Postgres JSON expression handler
 */
public class PostgresJsonExpression implements JsonExpressionHandler {

  @Override
  public void addSql(SpiExpressionRequest request, String propName, String path, Op operator, Object value) {

    StringBuilder sb = new StringBuilder(50);
    String[] paths = path.split("\\.");
    if (paths.length == 1) {
      // (t0.content ->> 'title') = 'Some value'
      sb.append("(").append(propName).append(" ->> '").append(path).append("')");

    } else {
      // (t0.content #>> '{path,inner}') = 'Some value'
      sb.append("(").append(propName).append(" #>> '{");
      for (int i = 0; i < paths.length; i++) {
        if (i > 0) {
          sb.append(",");
        }
        sb.append(paths[i]);
      }
      sb.append("}')");
    }

    request.append(castType(sb.toString(), value));
    request.append(operator.bind());
  }

  /**
   * Postgres CAST the type if necessary as text values always returned from the json operators used.
   */
  private String castType(String expression, Object value) {

    if (value == null) {
      // for exists and not-exists expressions
      return expression;
    }

    // Postgres cast of returned text value
    if (isIntegerType(value)) {
      return expression+"::INTEGER";
    }
    if (isNumberType(value)) {
      return expression+"::DECIMAL";
    }
    if (isBooleanType(value)) {
      return expression+"::BOOLEAN";
    }

    return expression;
  }

  private boolean isBooleanType(Object value) {
    return (value instanceof Boolean);
  }

  private boolean isIntegerType(Object value) {
    return (value instanceof Integer) || (value instanceof Long);
  }

  private boolean isNumberType(Object value) {
    return (value instanceof Number);
  }
}
