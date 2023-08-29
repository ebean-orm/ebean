package io.ebeaninternal.server.expression;

import io.ebean.event.BeanQueryRequest;
import io.ebeaninternal.api.*;
import io.ebeaninternal.server.deploy.BeanDescriptor;

/**
 * Effectively an expression that has no effect.
 */
final class NoopExpression implements SpiExpression {

  static final NoopExpression INSTANCE = new NoopExpression();

  @Override
  public void prefixProperty(String path) {
    // do nothing
  }

  @Override
  public boolean naturalKey(NaturalKeyQueryData<?> data) {
    // can't use naturalKey cache
    return false;
  }

  @Override
  public void simplify() {
    // do nothing
  }

  @Override
  public SpiExpression copyForPlanKey() {
    return this;
  }

  @Override
  public Object getIdEqualTo(String idName) {
    // always return null for this expression
    return null;
  }

  @Override
  public void containsMany(BeanDescriptor<?> desc, ManyWhereJoins whereManyJoins) {
    // nothing to do
  }

  @Override
  public void validate(SpiExpressionValidation validation) {
    // always valid
  }

  @Override
  public void prepareExpression(BeanQueryRequest<?> request) {
    // do nothing
  }

  @Override
  public void queryPlanHash(StringBuilder builder) {
    builder.append("Noop[]");
  }

  @Override
  public void queryBindKey(BindValuesKey key) {
    // no bind values
  }

  @Override
  public void addSql(SpiExpressionRequest request) {
    request.append(SQL_TRUE);
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {
    // nothing to do
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    return true;
  }
}
