package io.ebeaninternal.server.expression;

import io.ebeaninternal.api.SpiExpression;
import io.ebeaninternal.api.SpiExpressionRequest;

import java.io.IOException;

class InRangeExpression extends AbstractExpression {

  private final Object valueHigh;

  private final Object valueLow;

  InRangeExpression(String propertyName, Object valueLow, Object valueHigh) {
    super(propertyName);
    this.valueLow = valueLow;
    this.valueHigh = valueHigh;
  }

  private Object low() {
    return NamedParamHelp.value(valueLow);
  }

  private Object high() {
    return NamedParamHelp.value(valueHigh);
  }

  @Override
  public void writeDocQuery(DocQueryContext context) throws IOException {
    context.writeRange(propName, Op.GT_EQ, low(), Op.LT, high());
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {
    request.addBindValue(low());
    request.addBindValue(high());
  }

  @Override
  public void addSql(SpiExpressionRequest request) {
    request.append("(").append(propName).append(" >= ? and ").append(propName).append(" < ?)");
  }

  @Override
  public void queryPlanHash(StringBuilder builder) {
    builder.append("InRange[").append(propName).append("]");
  }

  @Override
  public int queryBindHash() {
    int hc = low().hashCode();
    hc = hc * 92821 + high().hashCode();
    return hc;
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    InRangeExpression that = (InRangeExpression) other;
    return low().equals(that.low()) && high().equals(that.high());
  }
}
