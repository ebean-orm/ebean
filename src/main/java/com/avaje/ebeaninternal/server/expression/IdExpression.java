package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.ManyWhereJoins;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.api.SpiExpressionValidation;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

/**
 * Slightly redundant as Query.setId() ultimately also does the same job.
 */
class IdExpression extends NonPrepareExpression implements SpiExpression {

  private static final long serialVersionUID = -3065936341718489842L;

  private final Object value;

  IdExpression(Object value) {
    this.value = value;
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

    // 'flatten' EmbeddedId and multiple Id cases
    // into an array of the underlying scalar field values
    DefaultExpressionRequest r = (DefaultExpressionRequest) request;
    Object[] bindIdValues = r.getBeanDescriptor().getBindIdValues(value);
    for (int i = 0; i < bindIdValues.length; i++) {
      request.addBindValue(bindIdValues[i]);
    }
  }

  @Override
  public void addSql(SpiExpressionRequest request) {

    DefaultExpressionRequest r = (DefaultExpressionRequest) request;
    String idSql = r.getBeanDescriptor().getIdBinderIdSql();

    request.append(idSql).append(" ");
  }

  /**
   * No properties so this is just a unique static number.
   */
  @Override
  public void queryPlanHash(HashQueryPlanBuilder builder) {
    builder.add(IdExpression.class);
    builder.bind(1);
  }

  @Override
  public int queryBindHash() {
    return value.hashCode();
  }

  @Override
  public boolean isSameByPlan(SpiExpression other) {
    return other instanceof IdExpression;
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    IdExpression that = (IdExpression) other;
    return value.equals(that.value);
  }
}
