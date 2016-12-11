package io.ebeaninternal.server.expression;

import io.ebean.event.BeanQueryRequest;
import io.ebeaninternal.api.HashQueryPlanBuilder;
import io.ebeaninternal.api.ManyWhereJoins;
import io.ebeaninternal.api.SpiExpression;
import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.api.SpiExpressionValidation;
import io.ebeaninternal.server.deploy.BeanDescriptor;

import java.io.IOException;

/**
 * Wraps a single expression with nestedPath for document queries.
 */
class NestedPathWrapperExpression implements SpiExpression {

  protected final String nestedPath;

  protected final SpiExpression delegate;

  NestedPathWrapperExpression(String nestedPath, SpiExpression delegate) {
    this.nestedPath = nestedPath;
    this.delegate = delegate;
  }

  @Override
  public void simplify() {
    // do nothing
  }

  @Override
  public void writeDocQuery(DocQueryContext context) throws IOException {
    context.startNested(nestedPath);
    delegate.writeDocQuery(context);
    context.endNested();
  }

  @Override
  public Object getIdEqualTo(String idName) {
    // always return null for this expression
    return null;
  }

  @Override
  public String nestedPath(BeanDescriptor<?> desc) {
    return nestedPath;
  }

  @Override
  public void containsMany(BeanDescriptor<?> desc, ManyWhereJoins whereManyJoins) {
    delegate.containsMany(desc, whereManyJoins);
  }

  @Override
  public void prepareExpression(BeanQueryRequest<?> request) {
    delegate.prepareExpression(request);
  }

  @Override
  public void queryPlanHash(HashQueryPlanBuilder builder) {
    delegate.queryPlanHash(builder);
  }

  @Override
  public int queryBindHash() {
    return delegate.queryBindHash();
  }

  @Override
  public boolean isSameByPlan(SpiExpression other) {
    if (other instanceof NestedPathWrapperExpression) {
      NestedPathWrapperExpression that = (NestedPathWrapperExpression) other;
      return nestedPath.equals(that.nestedPath)
        && delegate.isSameByPlan(that.delegate);
    }
    return false;
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    return delegate.isSameByBind(other);
  }

  @Override
  public void addSql(SpiExpressionRequest request) {
    delegate.addSql(request);
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {
    delegate.addBindValues(request);
  }

  @Override
  public void validate(SpiExpressionValidation validation) {
    delegate.validate(validation);
  }

  @Override
  public SpiExpression copyForPlanKey() {
    return new NestedPathWrapperExpression(nestedPath, delegate.copyForPlanKey());
  }
}
