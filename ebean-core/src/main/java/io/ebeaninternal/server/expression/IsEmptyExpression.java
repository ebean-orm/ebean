package io.ebeaninternal.server.expression;

import io.ebean.util.SplitName;
import io.ebeaninternal.api.BindValuesKey;
import io.ebeaninternal.api.ManyWhereJoins;
import io.ebeaninternal.api.SpiExpression;
import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.el.ElPropertyValue;

final class IsEmptyExpression extends AbstractExpression {

  private final boolean empty;
  private final String propertyPath;
  private String nestedPath;

  IsEmptyExpression(String propertyName, boolean empty) {
    super(propertyName);
    this.empty = empty;
    this.propertyPath = SplitName.split(propertyName)[0];
  }

  public String getPropName() {
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

    isEmptySql(request, prop, empty, propertyPath);
  }

  /**
   * Append an exists subQuery for the property.
   */
  static void isEmptySql(SpiExpressionRequest request, ElPropertyValue prop, boolean empty, String propertyPath) {
    if (empty) {
      request.append("not ");
    }
    request
      .append("exists (select 1 ")
      .parse(prop.assocIsEmpty(request, propertyPath))
      .append(')');
  }

  /**
   * Based on the type and propertyName.
   */
  @Override
  public void queryPlanHash(StringBuilder builder) {
    if (empty) {
      builder.append("IsEmpty[");
    } else {
      builder.append("IsNotEmpty[");
    }
    builder.append(propName).append(']');
  }

  @Override
  public void queryBindKey(BindValuesKey key) {
    // no bind values
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    return (other instanceof IsEmptyExpression);
  }
}
