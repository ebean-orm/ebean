package io.ebeaninternal.server.expression;

import io.ebeaninternal.api.ManyWhereJoins;
import io.ebeaninternal.api.SpiExpression;
import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.api.SpiExpressionValidation;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.el.ElPropertyDeploy;
import io.ebean.util.SplitName;

import java.io.IOException;

/**
 * Between expression where a value is between two properties.
 */
class BetweenPropertyExpression extends NonPrepareExpression {

  private static final String BETWEEN = " between ";

  private final String lowProperty;
  private final String highProperty;
  private final Object value;

  BetweenPropertyExpression(String lowProperty, String highProperty, Object value) {
    this.lowProperty = lowProperty;
    this.highProperty = highProperty;
    this.value = value;
  }

  protected String name(String propName) {
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
    ElPropertyDeploy elProp = desc.getElPropertyDeploy(name(lowProperty));
    if (elProp != null && elProp.containsMany()) {
      // assumes highProperty is also nested property which seems reasonable
      return SplitName.begin(lowProperty);
    }
    return null;
  }

  @Override
  public void containsMany(BeanDescriptor<?> desc, ManyWhereJoins manyWhereJoin) {

    ElPropertyDeploy elProp = desc.getElPropertyDeploy(name(lowProperty));
    if (elProp != null && elProp.containsMany()) {
      manyWhereJoin.add(elProp);
    }

    elProp = desc.getElPropertyDeploy(name(highProperty));
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
    request.append(" ?").append(BETWEEN).append(name(lowProperty)).append(" and ").append(name(highProperty)).append(" ");
  }

  @Override
  public void queryPlanHash(StringBuilder builder) {
    builder.append("BetweenProperties[").append("low:").append(lowProperty).append(" high:").append(highProperty).append("]");
  }

  @Override
  public int queryBindHash() {
    return val().hashCode();
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    BetweenPropertyExpression that = (BetweenPropertyExpression) other;
    return val().equals(that.val());
  }
}
