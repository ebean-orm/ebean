package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;

import java.io.IOException;


/**
 * Slightly redundant as Query.setId() ultimately also does the same job.
 */
class NullExpression extends AbstractExpression {

  private static final long serialVersionUID = 4246991057451128269L;

  private final boolean notNull;

  NullExpression(String propertyName, boolean notNull) {
    super(propertyName);
    this.notNull = notNull;
  }

  @Override
  public void writeElastic(ElasticExpressionContext context) throws IOException {
    context.writeExists(notNull, propName);
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {

  }

  @Override
  public void addSql(SpiExpressionRequest request) {

    String propertyName = propName;

    String nullExpr = notNull ? " is not null " : " is null ";

    ElPropertyValue prop = getElProp(request);
    if (prop != null && prop.isAssocId()) {
      request.append(prop.getAssocOneIdExpr(propertyName, nullExpr));
      return;
    }

    request.append(propertyName).append(nullExpr);
  }

  @Override
  public boolean isSameByPlan(SpiExpression other) {
    if (!(other instanceof NullExpression)) {
      return false;
    }

    NullExpression that = (NullExpression) other;
    return this.propName.equals(that.propName)
        && this.notNull == that.notNull;
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    // no bind values so always true
    return true;
  }

  /**
   * Based on notNull flag and the propertyName.
   */
  @Override
  public void queryPlanHash(HashQueryPlanBuilder builder) {
    builder.add(NullExpression.class).add(notNull).add(propName);
  }

  @Override
  public int queryBindHash() {
    return (notNull ? 1 : 0);
  }
}
