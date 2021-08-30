package io.ebeaninternal.server.expression;

import io.ebeaninternal.api.BindValuesKey;
import io.ebeaninternal.api.SpiExpression;
import io.ebeaninternal.api.SpiExpressionRequest;

import java.io.IOException;

/**
 * IsEmpty expression for ARRAY type.
 */
final class ArrayIsEmptyExpression extends AbstractExpression {

  private final boolean empty;

  ArrayIsEmptyExpression(String propName, boolean empty) {
    super(propName);
    this.empty = empty;
  }

  @Override
  public void writeDocQuery(DocQueryContext context) throws IOException {
    context.writeExists(!empty, propName);
  }

  @Override
  public void queryPlanHash(StringBuilder builder) {
    if (empty) {
      builder.append("ArrayIsEmpty[");
    } else {
      builder.append("ArrayIsNotEmpty[");
    }
    builder.append(propName).append("]");
  }

  @Override
  public void queryBindKey(BindValuesKey key) {
    key.add(empty);
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
