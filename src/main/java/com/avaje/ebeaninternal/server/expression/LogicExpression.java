package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebean.Expression;
import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.ManyWhereJoins;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.api.SpiExpressionValidation;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

/**
 * A logical And or Or for joining two expressions.
 */
abstract class LogicExpression implements SpiExpression {

  private static final long serialVersionUID = 616860781960645251L;

  static final String AND = " and ";
  static final String OR = " or ";

  static class And extends LogicExpression {

    private static final long serialVersionUID = -3832889676798526444L;

    And(Expression expOne, Expression expTwo) {
      super(AND, expOne, expTwo);
    }

    @Override
    public SpiExpression copyForPlanKey() {
      return new And(expOne.copyForPlanKey(), expTwo.copyForPlanKey());
    }

  }

  static class Or extends LogicExpression {

    private static final long serialVersionUID = -6871993143194094819L;

    Or(Expression expOne, Expression expTwo) {
      super(OR, expOne, expTwo);
    }

    @Override
    public SpiExpression copyForPlanKey() {
      return new Or(expOne.copyForPlanKey(), expTwo.copyForPlanKey());
    }
  }

  protected final SpiExpression expOne;

  protected final SpiExpression expTwo;

  private final String joinType;

  LogicExpression(String joinType, Expression expOne, Expression expTwo) {
    this.joinType = joinType;
    this.expOne = (SpiExpression) expOne;
    this.expTwo = (SpiExpression) expTwo;
  }

  @Override
  public void containsMany(BeanDescriptor<?> desc, ManyWhereJoins manyWhereJoin) {
    expOne.containsMany(desc, manyWhereJoin);
    expTwo.containsMany(desc, manyWhereJoin);
  }

  @Override
  public void validate(SpiExpressionValidation validation) {
    expOne.validate(validation);
    expTwo.validate(validation);
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {
    expOne.addBindValues(request);
    expTwo.addBindValues(request);
  }

  @Override
  public void addSql(SpiExpressionRequest request) {

    request.append("(");
    expOne.addSql(request);
    request.append(joinType);
    expTwo.addSql(request);
    request.append(") ");
  }

  @Override
  public void prepareExpression(BeanQueryRequest<?> request) {
    expOne.prepareExpression(request);
    expTwo.prepareExpression(request);
  }

  /**
   * Based on the joinType plus the two expressions.
   */
  @Override
  public void queryPlanHash(HashQueryPlanBuilder builder) {
    builder.add(LogicExpression.class).add(joinType);
    expOne.queryPlanHash(builder);
    expTwo.queryPlanHash(builder);
  }

  @Override
  public int queryBindHash() {
    int hc = expOne.queryBindHash();
    hc = hc * 31 + expTwo.queryBindHash();
    return hc;
  }

  @Override
  public boolean isSameByPlan(SpiExpression other) {

    if (!(other instanceof LogicExpression)) {
      return false;
    }

    LogicExpression that = (LogicExpression) other;
    return this.joinType.equals(that.joinType)
        && this.expOne.isSameByPlan(that.expOne)
        && this.expTwo.isSameByPlan(that.expTwo);
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    LogicExpression that = (LogicExpression) other;
    return this.expOne.isSameByBind(that.expOne)
        && this.expTwo.isSameByBind(that.expTwo);
  }

}
