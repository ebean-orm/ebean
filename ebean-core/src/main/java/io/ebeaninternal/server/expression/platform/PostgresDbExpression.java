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
    String[] paths = path.split("\\.");
    if (paths.length == 1) {
      // (t0.content ->> 'title') = 'Some value'
      request.append("(").parse(propName).append(" ->> '").append(path).append("')");
    } else {
      // (t0.content #>> '{path,inner}') = 'Some value'
      request.append("(").parse(propName).append(" #>> '{");
      for (int i = 0; i < paths.length; i++) {
        if (i > 0) {
          request.append(",");
        }
        request.append(paths[i]);
      }
      request.append("}')");
    }
    request.append(PostgresCast.cast(value)).append(operator.bind());
  }

  @Override
  public void arrayContains(SpiExpressionRequest request, String propName, boolean contains, Object... values) {
    if (!contains) {
      request.append("not (");
    }
    request.parse(propName).append(" @> array[?");
    for (int i = 1; i < values.length; i++) {
      request.append(",?");
    }
    request.append("]").append(PostgresCast.cast(values[0], true));
    if (!contains) {
      request.append(")");
    }
  }

  @Override
  public void arrayIsEmpty(SpiExpressionRequest request, String propName, boolean empty) {
    request.append("coalesce(cardinality(").parse(propName).append("),0)");
    if (empty) {
      request.append(" = 0");
    } else {
      request.append(" <> 0");
    }
  }

}
