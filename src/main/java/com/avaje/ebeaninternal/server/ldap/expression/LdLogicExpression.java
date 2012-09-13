package com.avaje.ebeaninternal.server.ldap.expression;

import com.avaje.ebean.Expression;
import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebeaninternal.api.ManyWhereJoins;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

/**
 * A logical And or Or for joining two expressions.
 */
abstract class LdLogicExpression implements SpiExpression {

  private static final long serialVersionUID = 616860781960645251L;

  static final String AND = "&";
  static final String OR = "|";

  static class And extends LdLogicExpression {

    private static final long serialVersionUID = -3832889676798526445L;

    And(Expression expOne, Expression expTwo) {
      super(AND, expOne, expTwo);
    }
  }

  static class Or extends LdLogicExpression {

    private static final long serialVersionUID = -6871993143194094810L;

    Or(Expression expOne, Expression expTwo) {
      super(OR, expOne, expTwo);
    }
  }

  private final SpiExpression expOne;

  private final SpiExpression expTwo;

  private final String joinType;

  LdLogicExpression(String joinType, Expression expOne, Expression expTwo) {
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
    request.append(joinType);
    expOne.addSql(request);
    expTwo.addSql(request);
    request.append(") ");
  }

  /**
   * Based on the joinType plus the two expressions.
   */
  public int queryAutoFetchHash() {
    int hc = LdLogicExpression.class.getName().hashCode() + joinType.hashCode();
    hc = hc * 31 + expOne.queryAutoFetchHash();
    hc = hc * 31 + expTwo.queryAutoFetchHash();
    return hc;
  }

  public int queryPlanHash(BeanQueryRequest<?> request) {
    int hc = LdLogicExpression.class.getName().hashCode() + joinType.hashCode();
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
