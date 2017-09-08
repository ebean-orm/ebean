package io.ebeaninternal.server.expression;

import io.ebeaninternal.api.SpiExpression;
import io.ebeaninternal.api.SpiExpressionRequest;

import java.io.IOException;

class BetweenExpression extends AbstractExpression {

  private static final String BETWEEN = " between ";

  private final Object valueHigh;

  private final Object valueLow;

  BetweenExpression(String propertyName, Object valueLow, Object valueHigh) {
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
    context.writeRange(propName, Op.GT_EQ, low(), Op.LT_EQ, high());
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {
    request.addBindValue(low());
    request.addBindValue(high());
  }

  @Override
  public void addSql(SpiExpressionRequest request) {
    request.append(propName).append(BETWEEN).append(" ? and ? ");
  }

  @Override
  public void queryPlanHash(StringBuilder builder) {
    builder.append("Between[").append(propName).append("]");
  }

  @Override
  public int queryBindHash() {
    int hc = low().hashCode();
    hc = hc * 92821 + high().hashCode();
    return hc;
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    BetweenExpression that = (BetweenExpression) other;
    return low().equals(that.low()) && high().equals(that.high());
  }
}
