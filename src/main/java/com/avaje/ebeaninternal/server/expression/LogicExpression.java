package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebean.Expression;
import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
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
  public void queryAutoFetchHash(HashQueryPlanBuilder builder) {
    builder.add(LogicExpression.class).add(joinType);    
    expOne.queryAutoFetchHash(builder);
    expTwo.queryAutoFetchHash(builder);
  }

  public void queryPlanHash(BeanQueryRequest<?> request, HashQueryPlanBuilder builder) {
    builder.add(LogicExpression.class).add(joinType);    
    expOne.queryPlanHash(request, builder);
    expTwo.queryPlanHash(request, builder);
  }

  public int queryBindHash() {
    int hc = expOne.queryBindHash();
    hc = hc * 31 + expTwo.queryBindHash();
    return hc;
  }

}
