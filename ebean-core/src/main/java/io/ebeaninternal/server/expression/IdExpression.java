package io.ebeaninternal.server.expression;

import io.ebeaninternal.api.*;
import io.ebeaninternal.server.deploy.BeanDescriptor;

/**
 * Slightly redundant as Query.setId() ultimately also does the same job.
 */
final class IdExpression extends NonPrepareExpression implements SpiExpression {

  private final Object value;

  IdExpression(Object value) {
    this.value = value;
  }

  @Override
  public void prefixProperty(String path) {
    throw new IllegalStateException("Not allowed?");
  }

  /**
   * Always returns false.
   */
  @Override
  public void containsMany(BeanDescriptor<?> desc, ManyWhereJoins manyWhereJoin) {

  }

  @Override
  public void validate(SpiExpressionValidation validation) {
    // always valid
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {
    // 'flatten' EmbeddedId and multiple ID cases
    // into an array of the underlying scalar field values
    for (Object bindIdValue : request.descriptor().bindIdValues(value)) {
      request.addBindValue(bindIdValue);
    }
  }

  @Override
  public void addSql(SpiExpressionRequest request) {
    request.parse(request.descriptor().idBinderIdSql(null));
  }

  /**
   * No properties so this is just a unique static number.
   */
  @Override
  public void queryPlanHash(StringBuilder builder) {
    builder.append("Id[]");
  }

  @Override
  public void queryBindKey(BindValuesKey key) {
    key.add(value);
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    IdExpression that = (IdExpression) other;
    return value.equals(that.value);
  }
}
