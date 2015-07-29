package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.ManyWhereJoins;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

/**
 * Effectively an expression that has no effect.
 */
class NoopExpression implements SpiExpression {

  protected static final NoopExpression INSTANCE = new NoopExpression();

  @Override
  public void containsMany(BeanDescriptor<?> desc, ManyWhereJoins whereManyJoins) {
    // nothing to do
  }

  @Override
  public void queryAutoFetchHash(HashQueryPlanBuilder builder) {
    builder.add(NoopExpression.class);
  }

  @Override
  public void queryPlanHash(BeanQueryRequest<?> request, HashQueryPlanBuilder builder) {
    queryAutoFetchHash(builder);
  }

  @Override
  public int queryBindHash() {
    // no bind values
    return 0;
  }

  @Override
  public void addSql(SpiExpressionRequest request) {
    request.append("1=1");
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {
    // nothing to do
  }
}
