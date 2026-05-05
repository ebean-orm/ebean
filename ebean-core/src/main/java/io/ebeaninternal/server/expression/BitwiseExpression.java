package io.ebeaninternal.server.expression;

import io.ebeaninternal.api.BindValuesKey;
import io.ebeaninternal.api.SpiExpression;
import io.ebeaninternal.api.SpiExpressionBind;
import io.ebeaninternal.api.SpiExpressionRequest;

/**
 * Bitwise expression.
 */
final class BitwiseExpression extends AbstractExpression {

  private final BitwiseOp operator;
  private final String compare;
  private final long flags;
  private final long match;

  BitwiseExpression(String propertyName, BitwiseOp operator, long flags, String compare, long match) {
    super(propertyName);
    this.operator = operator;
    this.flags = flags;
    this.compare = compare;
    this.match = match;
  }

  @Override
  public void writeDocQuery(DocQueryContext context) {
    throw new IllegalStateException("Not supported for document queries");
  }

  @Override
  public void queryPlanHash(StringBuilder builder) {
    builder.append("Bitwise[");
    builder.append(propName).append(" op:").append(operator).append(" cp:").append(compare);
    builder.append(" ?2]");
  }

  @Override
  public void queryBindKey(BindValuesKey key) {
    key.add(flags).add(match);
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    BitwiseExpression that = (BitwiseExpression) other;
    return operator == that.operator && compare.equals(that.compare) && flags == that.flags && match == that.match;
  }

  @Override
  public void addSql(SpiExpressionRequest request) {
    // Use DB specific expression handling
    request.platformHandler().bitwise(request, propName, operator, flags, compare, match);
  }

  @Override
  public void addBindValues(SpiExpressionBind request) {
    request.addBindValue(flags);
    request.addBindValue(match);
  }
}
