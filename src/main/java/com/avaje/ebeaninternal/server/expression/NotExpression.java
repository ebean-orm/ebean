package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebean.Expression;
import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.ManyWhereJoins;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
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

  public void addBindValues(SpiExpressionRequest request) {
    exp.addBindValues(request);
  }

  public void addSql(SpiExpressionRequest request) {
    request.append(NOT);
    exp.addSql(request);
    request.append(") ");
  }

  /**
   * Based on the expression.
   */
  public void queryAutoFetchHash(HashQueryPlanBuilder builder) {
    builder.add(NotExpression.class);
    exp.queryAutoFetchHash(builder);
  }

  public void queryPlanHash(BeanQueryRequest<?> request, HashQueryPlanBuilder builder) {
    builder.add(NotExpression.class);
    exp.queryPlanHash(request, builder);
  }

  public int queryBindHash() {
    return exp.queryBindHash();
  }

}
