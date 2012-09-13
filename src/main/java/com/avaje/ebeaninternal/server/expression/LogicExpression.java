package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebean.Expression;
import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebeaninternal.api.ManyWhereJoins;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
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
  }

  static class Or extends LogicExpression {

    private static final long serialVersionUID = -6871993143194094819L;

    Or(Expression expOne, Expression expTwo) {
      super(OR, expOne, expTwo);
    }
  }

  private final SpiExpression expOne;

  private final SpiExpression expTwo;

  private final String joinType;

  LogicExpression(String joinType, Expression expOne, Expression expTwo) {
    this.joinType = joinType;
    this.expOne = (SpiExpression) expOne;
    this.expTwo = (SpiExpression) expTwo;
  }

  public void containsMany(BeanDescriptor<?> desc, ManyWhereJoins manyWhereJoin) {
    expOne.containsMany(desc, manyWhereJoin);
    expTwo.containsMany(desc, manyWhereJoin);
  }

  public void addBindValues(SpiExpressionRequest request) {
    expOne.addBindValues(request);
    expTwo.addBindValues(request);
  }

  public void addSql(SpiExpressionRequest request) {

    request.append("(");
    expOne.addSql(request);
    request.append(joinType);
    expTwo.addSql(request);
    request.append(") ");
  }

  /**
   * Based on the joinType plus the two expressions.
   */
  public int queryAutoFetchHash() {
    int hc = LogicExpression.class.getName().hashCode() + joinType.hashCode();
    hc = hc * 31 + expOne.queryAutoFetchHash();
    hc = hc * 31 + expTwo.queryAutoFetchHash();
    return hc;
  }

  public int queryPlanHash(BeanQueryRequest<?> request) {
    int hc = LogicExpression.class.getName().hashCode() + joinType.hashCode();
    hc = hc * 31 + expOne.queryPlanHash(request);
    hc = hc * 31 + expTwo.queryPlanHash(request);
    return hc;
  }

  public int queryBindHash() {
    int hc = expOne.queryBindHash();
    hc = hc * 31 + expTwo.queryBindHash();
    return hc;
  }

}
