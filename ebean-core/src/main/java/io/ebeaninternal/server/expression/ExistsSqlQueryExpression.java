package io.ebeaninternal.server.expression;

import io.ebean.event.BeanQueryRequest;
import io.ebeaninternal.api.*;
import io.ebeaninternal.server.deploy.BeanDescriptor;

import java.util.Arrays;

final class ExistsSqlQueryExpression implements SpiExpression, UnsupportedDocStoreExpression {

  private final boolean not;
  private final String subQuery;
  private final Object[] bindParams;

  ExistsSqlQueryExpression(boolean not, String subQuery, Object[] bindParams) {
    this.not = not;
    this.subQuery = subQuery;
    this.bindParams = bindParams;
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
  public void writeDocQuery(DocQueryContext context) {
    throw new IllegalStateException("Not supported");
  }

  @Override
  public Object getIdEqualTo(String idName) {
    // always return null for this expression
    return null;
  }

  @Override
  public void prepareExpression(BeanQueryRequest<?> request) {
    // do nothing
  }

  @Override
  public SpiExpression copyForPlanKey() {
    return this;
  }

  @Override
  public void queryPlanHash(StringBuilder builder) {
    builder.append("ExistsSqlQuery[").append(" not:").append(not);
    builder.append(" sql:").append(subQuery).append(" ?:").append(bindParams.length).append(']');
  }

  @Override
  public void queryBindKey(BindValuesKey key) {
    for (Object value : bindParams) {
      key.add(value);
    }
  }

  @Override
  public void addSql(SpiExpressionRequest request) {
    if (not) {
      request.append("not ");
    }
    request.append("exists (").parse(subQuery).append(')');
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {
    for (Object bindParam : bindParams) {
      request.addBindValue(bindParam);
    }
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    final ExistsSqlQueryExpression that = (ExistsSqlQueryExpression) other;
    return Arrays.equals(bindParams, that.bindParams);
  }

  @Override
  public String nestedPath(BeanDescriptor<?> desc) {
    return null;
  }

  @Override
  public void containsMany(BeanDescriptor<?> desc, ManyWhereJoins whereManyJoins) {
    // Nothing to do for exists expression
  }

  @Override
  public void validate(SpiExpressionValidation validation) {
    // Nothing to do for exists expression
  }
}
