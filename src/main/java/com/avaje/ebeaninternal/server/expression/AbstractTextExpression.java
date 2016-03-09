package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;

/**
 * Base class for TextExpressions that are only executable by doc store.
 * <p>
 * This means they can not be part of a SQL query nor do they use the built in query plan cache etc.
 * </p>
 */
public abstract class AbstractTextExpression extends AbstractExpression {

  protected AbstractTextExpression(String propName) {
    super(propName);
  }

  @Override
  public void addSql(SpiExpressionRequest request) {
    throw new IllegalStateException("Not implemented - DocStore/Elastic only");
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {
    throw new IllegalStateException("Not implemented - DocStore/Elastic only");
  }

  @Override
  public void queryPlanHash(HashQueryPlanBuilder builder) {
    throw new IllegalStateException("Not implemented - query plan caching done explicitly by the doc store");
  }

  @Override
  public int queryBindHash() {
    throw new IllegalStateException("Not implemented - query plan caching done explicitly by the doc store");
  }

  @Override
  public boolean isSameByPlan(SpiExpression other) {
    throw new IllegalStateException("Not implemented - query plan caching done explicitly by the doc store");
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    throw new IllegalStateException("Not implemented - query plan caching done explicitly by the doc store");
  }
}
