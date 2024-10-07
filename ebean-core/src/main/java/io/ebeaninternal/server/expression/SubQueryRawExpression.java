package io.ebeaninternal.server.expression;

import io.ebeaninternal.api.BindValuesKey;
import io.ebeaninternal.api.SpiExpression;
import io.ebeaninternal.api.SpiExpressionBind;
import io.ebeaninternal.api.SpiExpressionRequest;

import java.util.Arrays;

/**
 * Raw SQL based Sub-Query expression.
 */
final class SubQueryRawExpression extends AbstractExpression implements UnsupportedDocStoreExpression {

  private final SubQueryOp op;
  private final String subQuery;
  private final Object[] bindParams;

  SubQueryRawExpression(SubQueryOp op, String propertyName, String subQuery, Object[] bindParams) {
    super(propertyName);
    this.op = op;
    this.subQuery = subQuery;
    this.bindParams = bindParams;
  }

  @Override
  public void writeDocQuery(DocQueryContext context) {
    throw new IllegalStateException("Not supported");
  }

  @Override
  public void queryPlanHash(StringBuilder builder) {
    builder.append("SubQueryRaw[").append(propName).append(op.expression)
      .append(" subQuery:").append(subQuery)
      .append(" ?:").append(bindParams.length).append(']');
  }

  @Override
  public void queryBindKey(BindValuesKey key) {
    for (Object value : bindParams) {
      key.add(value);
    }
  }

  @Override
  public void addSql(SpiExpressionRequest request) {
    request.property(propName).append(op.expression).append('(').append(subQuery).append(')');
  }

  @Override
  public void addBindValues(SpiExpressionBind request) {
    for (Object bindParam : bindParams) {
      request.addBindValue(bindParam);
    }
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    final SubQueryRawExpression that = (SubQueryRawExpression) other;
    return Arrays.equals(bindParams, that.bindParams);
  }
}
