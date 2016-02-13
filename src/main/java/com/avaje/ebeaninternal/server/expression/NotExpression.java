package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebean.Expression;
import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.ManyWhereJoins;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.api.SpiExpressionValidation;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

final class NotExpression implements SpiExpression {

  private static final long serialVersionUID = 5648926732402355781L;

  private static final String NOT = "not (";

  private final SpiExpression exp;

  NotExpression(Expression exp) {
    this.exp = (SpiExpression) exp;
  }

  public void containsMany(BeanDescriptor<?> desc, ManyWhereJoins manyWhereJoin) {
    exp.containsMany(desc, manyWhereJoin);
  }

  @Override
  public void validate(SpiExpressionValidation validation) {
    exp.validate(validation);
  }

  public void addBindValues(SpiExpressionRequest request) {
    exp.addBindValues(request);
  }

  public void addSql(SpiExpressionRequest request) {
    request.append(NOT);
    exp.addSql(request);
    request.append(") ");
  }

  @Override
  public void prepareExpression(BeanQueryRequest<?> request) {
    exp.prepareExpression(request);
  }

  /**
   * Based on the expression.
   */
  @Override
  public void queryPlanHash(HashQueryPlanBuilder builder) {
    builder.add(NotExpression.class);
    exp.queryPlanHash(builder);
  }

  public int queryBindHash() {
    return exp.queryBindHash();
  }

}
