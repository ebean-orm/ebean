package io.ebeaninternal.server.expression;

import io.ebean.event.BeanQueryRequest;
import io.ebeaninternal.api.*;
import io.ebeaninternal.server.deploy.BeanDescriptor;

import java.io.IOException;

/**
 * Wraps a single expression with nestedPath for document queries.
 */
final class NestedPathWrapperExpression implements SpiExpression {

  final String nestedPath;
  final SpiExpression delegate;

  NestedPathWrapperExpression(String nestedPath, SpiExpression delegate) {
    this.nestedPath = nestedPath;
    this.delegate = delegate;
  }

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
  public void queryPlanHash(StringBuilder builder) {
    builder.append("NestedPath[");
    if (nestedPath != null) {
      builder.append("path:").append(nestedPath).append(' ');
    }
    delegate.queryPlanHash(builder);
    builder.append(']');
  }

  @Override
  public void queryBindKey(BindValuesKey key) {
    delegate.queryBindKey(key);
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
  public void addBindValues(SpiExpressionBind request) {
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
