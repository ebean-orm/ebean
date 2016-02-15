package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;

class BetweenExpression extends AbstractExpression {

  private static final long serialVersionUID = 2078918165221454910L;

  private static final String BETWEEN = " between ";

  private final Object valueHigh;

  private final Object valueLow;

  BetweenExpression(String propertyName, Object valLo, Object valHigh) {
    super(propertyName);
    this.valueLow = valLo;
    this.valueHigh = valHigh;
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {
    request.addBindValue(valueLow);
    request.addBindValue(valueHigh);
  }

  @Override
  public void addSql(SpiExpressionRequest request) {

    request.append(propName).append(BETWEEN).append(" ? and ? ");
  }

  @Override
  public void queryPlanHash(HashQueryPlanBuilder builder) {
    builder.add(BetweenExpression.class).add(propName);
    builder.bind(2);
  }

  @Override
  public int queryBindHash() {
    int hc = valueLow.hashCode();
    hc = hc * 31 + valueHigh.hashCode();
    return hc;
  }

  @Override
  public boolean isSameByPlan(SpiExpression other) {
    if (!(other instanceof BetweenExpression)) {
      return false;
    }

    BetweenExpression that = (BetweenExpression) other;
    return this.propName.equals(that.propName);
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    BetweenExpression that = (BetweenExpression) other;
    return valueLow.equals(that.valueLow)
        && valueHigh.equals(that.valueHigh);
  }
}
