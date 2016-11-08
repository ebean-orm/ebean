package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;

import java.io.IOException;

/**
 * IsEmpty expression for ARRAY type.
 */
public class ArrayIsEmptyExpression extends AbstractExpression {

  private final boolean empty;

  protected ArrayIsEmptyExpression(String propName, boolean empty) {
    super(propName);
    this.empty = empty;
  }

  @Override
  public void writeDocQuery(DocQueryContext context) throws IOException {
    context.writeExists(!empty, propName);
  }

  @Override
  public void queryPlanHash(HashQueryPlanBuilder builder) {
    builder.add(ArrayIsEmptyExpression.class).add(propName);
  }

  @Override
  public int queryBindHash() {
    return empty ? 0 : 92821;
  }

  @Override
  public boolean isSameByPlan(SpiExpression other) {
    if (!(other instanceof ArrayIsEmptyExpression)) {
      return false;
    }
    ArrayIsEmptyExpression that = (ArrayIsEmptyExpression) other;
    return this.propName.equals(that.propName)
      && this.empty == that.empty;
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    return true;
  }

  @Override
  public void addSql(SpiExpressionRequest request) {
    request.getDbPlatformHandler().arrayIsEmpty(request, propName, empty);
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {
    // nothing to bind
  }
}
