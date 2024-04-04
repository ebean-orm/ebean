package io.ebeaninternal.server.expression;

import io.ebeaninternal.api.*;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssoc;
import io.ebeaninternal.server.deploy.DeployPropertyParser;
import io.ebeaninternal.server.persist.MultiValueWrapper;

import java.util.Collection;

final class RawExpression extends NonPrepareExpression {

  final String sql;
  final Object[] values;
  private String prefixPath;

  RawExpression(String sql, Object[] values) {
    this.sql = sql;
    this.values = values;
  }

  @Override
  public void containsMany(BeanDescriptor<?> desc, ManyWhereJoins manyWhereJoin) {

  }

  @Override
  public void validate(SpiExpressionValidation validation) {
    // always ignored
  }

  @Override
  public void addBindValues(SpiExpressionBind request) {
    if (values != null) {
      for (Object value : values) {
        if (value instanceof Collection<?>) {
          // support for Postgres = any(?) type raw expression
          request.addBindValue(new MultiValueWrapper((Collection<?>)value));
        } else {
          request.addBindValue(value);
        }
      }
    }
  }

  @Override
  public void prefixProperty(String path) {
    this.prefixPath = path;
  }

  @Override
  public void addSql(SpiExpressionRequest request) {
    if (prefixPath == null) {
      request.parse(sql);
    } else {
      BeanDescriptor<?> descriptor = request.descriptor();
      BeanProperty beanProperty = descriptor.findPropertyFromPath(prefixPath);
      BeanPropertyAssoc bpa = (BeanPropertyAssoc)beanProperty;
      DeployPropertyParser parser = bpa.targetDescriptor().parser();
      request.append(filterManyPaths(prefixPath, parser.parse(sql)));
    }
  }


  static String filterManyPaths(String prefix, String raw) {
    final StringBuilder sb = new StringBuilder(raw.length() + 50);
    int lastPos = 0;
    int nextPos = raw.indexOf("${");
    while (nextPos > -1) {
      sb.append(raw.substring(lastPos, nextPos)).append("${").append(prefix);
      if (raw.charAt(nextPos + 2) != '}') {
        sb.append('.');
      }
      lastPos = nextPos + 2;
      nextPos = raw.indexOf("${", nextPos + 2);
    }
    sb.append(raw.substring(lastPos));
    return sb.toString();
  }

  /**
   * Based on the sql.
   */
  @Override
  public void queryPlanHash(StringBuilder builder) {
    builder.append("Raw[").append(sql);
    if (values != null) {
      builder.append(" ?").append(values.length);
    }
    builder.append(']');
  }

  @Override
  public void queryBindKey(BindValuesKey key) {
    key.add(values.length);
    for (Object value : values) {
      key.add(value);
    }
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    if (!(other instanceof RawExpression)) {
      return false;
    }

    RawExpression that = (RawExpression) other;
    if (values.length != that.values.length) {
      return false;
    }
    for (int i = 0; i < values.length; i++) {
      if (!Same.sameByValue(values[i], that.values[i])) {
        return false;
      }
    }
    return true;
  }
}
