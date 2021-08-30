package io.ebeaninternal.server.expression.platform;

import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.server.expression.Op;

/**
 * Postgres JSON and ARRAY expression handler
 */
final class PostgresDbExpression extends BaseDbExpression {

  @Override
  public String concat(String property0, String separator, String property1, String suffix) {
    return concatOperator(property0, separator, property1, suffix);
  }

  @Override
  public void json(SpiExpressionRequest request, String propName, String path, Op operator, Object value) {
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

    request.append(sb.toString());
    request.append(PostgresCast.cast(value));
    request.append(operator.bind());
  }

  @Override
  public void arrayContains(SpiExpressionRequest request, String propName, boolean contains, Object... values) {
    if (!contains) {
      request.append("not (");
    }
    request.append(propName).append(" @> array[?");
    for (int i = 1; i < values.length; i++) {
      request.append(",?");
    }
    request.append("]");
    request.append(PostgresCast.cast(values[0], true));
    if (!contains) {
      request.append(")");
    }
  }

  @Override
  public void arrayIsEmpty(SpiExpressionRequest request, String propName, boolean empty) {
    request.append("coalesce(cardinality(").append(propName).append("),0)");
    if (empty) {
      request.append(" = 0");
    } else {
      request.append(" <> 0");
    }
  }

}
