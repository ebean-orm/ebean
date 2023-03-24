package io.ebeaninternal.server.expression;

import io.ebean.util.SplitName;
import io.ebeaninternal.api.*;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.el.ElPropertyDeploy;

import java.io.IOException;

/**
 * Between expression where a value is between two properties.
 */
final class BetweenPropertyExpression extends NonPrepareExpression {

  private static final String BETWEEN = " between ";

  private String lowProperty;
  private String highProperty;
  private final Object value;

  BetweenPropertyExpression(String lowProperty, String highProperty, Object value) {
    this.lowProperty = lowProperty;
    this.highProperty = highProperty;
    this.value = value;
  }

  @Override
  public void prefixProperty(String path) {
    this.lowProperty = path + "." + lowProperty;
    this.highProperty = path + "." + highProperty;
  }

  String name(String propName) {
    return propName;
  }

  private Object val() {
    return NamedParamHelp.value(value);
  }

  @Override
  public void writeDocQuery(DocQueryContext context) throws IOException {
    context.startBoolMust();
    context.writeSimple(Op.LT_EQ, lowProperty, val());
    context.writeSimple(Op.GT_EQ, highProperty, val());
    context.endBool();
  }

  @Override
  public String nestedPath(BeanDescriptor<?> desc) {
    ElPropertyDeploy elProp = desc.elPropertyDeploy(name(lowProperty));
    if (elProp != null && elProp.containsMany()) {
      // assumes highProperty is also nested property which seems reasonable
      return SplitName.begin(lowProperty);
    }
    return null;
  }

  @Override
  public void containsMany(BeanDescriptor<?> desc, ManyWhereJoins manyWhereJoin) {
    ElPropertyDeploy elProp = desc.elPropertyDeploy(name(lowProperty));
    if (elProp != null && elProp.containsMany()) {
      manyWhereJoin.add(elProp);
    }
    elProp = desc.elPropertyDeploy(name(highProperty));
    if (elProp != null && elProp.containsMany()) {
      manyWhereJoin.add(elProp);
    }
  }

  @Override
  public void validate(SpiExpressionValidation validation) {
    validation.validate(lowProperty);
    validation.validate(highProperty);
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {
    request.addBindValue(val());
  }

  @Override
  public void addSql(SpiExpressionRequest request) {
    request.append(" ?").append(BETWEEN).parse(name(lowProperty)).append(" and ").parse(name(highProperty));
  }

  @Override
  public void queryPlanHash(StringBuilder builder) {
    builder.append("BetweenProperties[").append("low:").append(lowProperty).append(" high:").append(highProperty).append("]");
  }

  @Override
  public void queryBindKey(BindValuesKey key) {
    key.add(val());
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    BetweenPropertyExpression that = (BetweenPropertyExpression) other;
    return val().equals(that.val());
  }
}
