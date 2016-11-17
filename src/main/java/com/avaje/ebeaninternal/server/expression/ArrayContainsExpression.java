package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;

import java.io.IOException;

/**
 * Contains expression for ARRAY type.
 */
public class ArrayContainsExpression extends AbstractExpression {

  private final boolean contains;

  private final Object[] values;

  protected ArrayContainsExpression(String propName, boolean contains, Object... values) {
    super(propName);
    this.contains = contains;
    this.values = values;
    if (values == null || values.length == 0) {
      throw new IllegalArgumentException("values must not be null or empty");
    }
  }

  @Override
  public void writeDocQuery(DocQueryContext context) throws IOException {

    if (values.length == 1) {
      context.writeEqualTo(propName, values[0]);
    } else {
      if (contains) {
        context.startBoolMust();
      } else {
        context.startBoolMustNot();
      }
      for (Object value : values) {
        context.writeEqualTo(propName, value);
      }
      context.endBool();
    }
  }

  @Override
  public void queryPlanHash(HashQueryPlanBuilder builder) {
    builder.add(ArrayContainsExpression.class).add(propName).add(contains);
    builder.bind(values.length);
  }

  @Override
  public int queryBindHash() {
    int hc = values[0].hashCode();
    for (int i = 1; i < values.length; i++) {
      hc = hc * 92821 + values[i].hashCode();
    }
    return hc;
  }

  @Override
  public boolean isSameByPlan(SpiExpression other) {
    if (!(other instanceof ArrayContainsExpression)) {
      return false;
    }
    ArrayContainsExpression that = (ArrayContainsExpression) other;
    return this.propName.equals(that.propName)
      && this.contains == that.contains
      && this.values.length == that.values.length;
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    ArrayContainsExpression that = (ArrayContainsExpression) other;
    for (int i = 0; i < this.values.length; i++) {
      if (!this.values[i].equals(that.values[i])) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void addSql(SpiExpressionRequest request) {
    request.getDbPlatformHandler().arrayContains(request, propName, contains, values);
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {
    for (Object value : values) {
      request.addBindValue(value);
    }
  }
}
