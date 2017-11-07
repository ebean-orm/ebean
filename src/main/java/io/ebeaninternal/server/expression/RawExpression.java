package io.ebeaninternal.server.expression;

import io.ebeaninternal.api.ManyWhereJoins;
import io.ebeaninternal.api.SpiExpression;
import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.api.SpiExpressionValidation;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.persist.MultiValueWrapper;

import java.io.IOException;
import java.util.Collection;

class RawExpression extends NonPrepareExpression {

  private final String sql;

  private final Object[] values;

  RawExpression(String sql, Object[] values) {
    this.sql = sql;
    this.values = values;
  }

  @Override
  public void writeDocQuery(DocQueryContext context) throws IOException {
    context.writeRaw(sql, values);
  }

  @Override
  public String nestedPath(BeanDescriptor<?> desc) {
    return null;
  }

  @Override
  public void containsMany(BeanDescriptor<?> desc, ManyWhereJoins manyWhereJoin) {

  }

  @Override
  public void validate(SpiExpressionValidation validation) {
    // always ignored
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {
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
  public void addSql(SpiExpressionRequest request) {
    request.append(sql);
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
    builder.append("]");
  }

  @Override
  public int queryBindHash() {
    return sql.hashCode();
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
