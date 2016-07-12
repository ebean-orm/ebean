package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebean.Expression;
import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.ManyWhereJoins;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.api.SpiExpressionValidation;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

import java.io.IOException;

final class NotExpression implements SpiExpression {

  private static final String NOT_START = "not (";
  private static final String NOT_END = ") ";

  private final SpiExpression exp;

  NotExpression(Expression exp) {
    this.exp = (SpiExpression) exp;
  }

  @Override
  public void simplify() {
    // do nothing
  }

  @Override
  public void writeDocQuery(DocQueryContext context) throws IOException {
    context.startBoolMustNot();
    exp.writeDocQuery(context);
    context.endBool();
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
  public String nestedPath(BeanDescriptor<?> desc) {
    return exp.nestedPath(desc);
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
  public void queryPlanHash(HashQueryPlanBuilder builder) {
    builder.add(NotExpression.class);
    exp.queryPlanHash(builder);
  }

  @Override
  public int queryBindHash() {
    return exp.queryBindHash();
  }

  @Override
  public boolean isSameByPlan(SpiExpression other) {
    if (!(other instanceof NotExpression)) {
      return false;
    }
    NotExpression that = (NotExpression) other;
    return exp.isSameByPlan(that.exp);
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    NotExpression that = (NotExpression) other;
    return exp.isSameByBind(that.exp);
  }
}
