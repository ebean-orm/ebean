package io.ebeaninternal.server.expression;

import io.ebean.event.BeanQueryRequest;
import io.ebeaninternal.api.ManyWhereJoins;
import io.ebeaninternal.api.SpiExpression;
import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.api.SpiExpressionValidation;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.api.NaturalKeyQueryData;

import java.io.IOException;

/**
 * Effectively an expression that has no effect.
 */
class NoopExpression implements SpiExpression {

  protected static final NoopExpression INSTANCE = new NoopExpression();

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
  public void writeDocQuery(DocQueryContext context) throws IOException {
  }

  @Override
  public Object getIdEqualTo(String idName) {
    // always return null for this expression
    return null;
  }

  @Override
  public String nestedPath(BeanDescriptor<?> desc) {
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
  public int queryBindHash() {
    // no bind values
    return 0;
  }

  @Override
  public void addSql(SpiExpressionRequest request) {
    request.append("1=1 ");
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
