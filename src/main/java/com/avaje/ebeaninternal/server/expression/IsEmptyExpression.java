package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.ManyWhereJoins;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;
import com.avaje.ebeaninternal.server.query.SplitName;

import java.io.IOException;

public class IsEmptyExpression extends AbstractExpression {

  private final boolean empty;

  private final String propertyPath;

  public IsEmptyExpression(String propertyName, boolean empty) {
    super(propertyName);
    this.empty = empty;
    this.propertyPath = SplitName.split(propertyName)[0];
  }

  @Override
  public void writeDocQuery(DocQueryContext context) throws IOException {

  }

  public final String getPropName() {
    return propName;
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {
    // no bind values
  }

  @Override
  public void containsMany(BeanDescriptor<?> desc, ManyWhereJoins manyWhereJoin) {
    // we don't want the extra join
    propertyContainsMany(propertyPath, desc, manyWhereJoin);
  }

  @Override
  public void addSql(SpiExpressionRequest request) {

    ElPropertyValue prop = getElProp(request);
    if (prop == null) {
      throw new IllegalStateException("Property [" + propName + "] not found");
    }

    if (empty) {
      request.append("not ");
    }

    request
        .append("exists (select 1 from ")
        .append(prop.getAssocIsEmpty(request, propertyPath))
        .append(")");
  }

  /**
   * Based on the type and propertyName.
   */
  @Override
  public void queryPlanHash(HashQueryPlanBuilder builder) {
    builder.add(IsEmptyExpression.class).add(propName);
  }

  @Override
  public int queryBindHash() {
    return 1;
  }

  @Override
  public boolean isSameByPlan(SpiExpression other) {
    if (!(other instanceof IsEmptyExpression)) {
      return false;
    }

    IsEmptyExpression that = (IsEmptyExpression) other;
    return this.propName.equals(that.propName)
        && this.empty == that.empty;
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    return (other instanceof IsEmptyExpression);
  }
}
