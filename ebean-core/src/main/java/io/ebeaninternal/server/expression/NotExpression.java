package io.ebeaninternal.server.expression;

import io.ebean.Expression;
import io.ebean.event.BeanQueryRequest;
import io.ebeaninternal.api.BindValuesKey;
import io.ebeaninternal.api.ManyWhereJoins;
import io.ebeaninternal.api.NaturalKeyQueryData;
import io.ebeaninternal.api.SpiExpression;
import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.api.SpiExpressionValidation;
import io.ebeaninternal.server.deploy.BeanDescriptor;

final class NotExpression implements SpiExpression {

  private static final String NOT_START = "not (";
  private static final String NOT_END = ")";

  private final SpiExpression exp;

  NotExpression(Expression exp) {
    this.exp = (SpiExpression) exp;
  }

  @Override
  public void prefixProperty(String path) {
    exp.prefixProperty(path);
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
  public Object getIdEqualTo(String idName) {
    // always return null for this expression
    return null;
  }

  @Override
  public SpiExpression copyForPlanKey() {
    return new NotExpression(exp.copyForPlanKey());
  }

  @Override
  public void containsMany(BeanDescriptor<?> desc, ManyWhereJoins manyWhereJoin) {
    exp.containsMany(desc, manyWhereJoin);
  }

  @Override
  public void validate(SpiExpressionValidation validation) {
    exp.validate(validation);
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {
    exp.addBindValues(request);
  }

  @Override
  public void addSql(SpiExpressionRequest request) {
    request.append(NOT_START);
    exp.addSql(request);
    request.append(NOT_END);
  }

  @Override
  public void prepareExpression(BeanQueryRequest<?> request) {
    exp.prepareExpression(request);
  }

  /**
   * Based on the expression.
   */
  @Override
  public void queryPlanHash(StringBuilder builder) {
    builder.append("Not[");
    exp.queryPlanHash(builder);
    builder.append(']');
  }

  @Override
  public void queryBindKey(BindValuesKey key) {
    exp.queryBindKey(key);
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    NotExpression that = (NotExpression) other;
    return exp.isSameByBind(that.exp);
  }
}
