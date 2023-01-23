package io.ebeaninternal.server.expression;

import io.ebeaninternal.api.BindValuesKey;
import io.ebeaninternal.api.SpiExpression;
import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.server.el.ElPropertyValue;

import java.io.IOException;

final class InRangeExpression extends AbstractExpression {

  private final Object valueHigh;
  private final Object valueLow;

  InRangeExpression(String propertyName, Object valueLow, Object valueHigh) {
    super(propertyName);
    this.valueLow = valueLow;
    this.valueHigh = valueHigh;
  }

  private Object low() {
    return NamedParamHelp.value(valueLow);
  }

  private Object high() {
    return NamedParamHelp.value(valueHigh);
  }

  @Override
  public void writeDocQuery(DocQueryContext context) throws IOException {
    context.writeRange(propName, Op.GT_EQ, low(), Op.LT, high());
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {
    ElPropertyValue prop = getElProp(request);
    if (prop != null && prop.isDbEncrypted()) {
      // bind the key twice, for both values
      String encryptKey = prop.beanProperty().encryptKey().getStringValue();
      request.addBindEncryptKey(encryptKey);
      request.addBindValue(low());
      request.addBindEncryptKey(encryptKey);
      request.addBindValue(high());
      return;
    }
    request.addBindValue(low());
    request.addBindValue(high());

  }

  @Override
  public void addSql(SpiExpressionRequest request) {
    String pname = propName;
    ElPropertyValue prop = getElProp(request);
    if (prop != null && prop.isDbEncrypted()) {
      pname = prop.beanProperty().decryptProperty(propName);
    }
    request.append("(").append(pname).append(" >= ? and ").append(pname).append(" < ?)");
  }

  @Override
  public void queryPlanHash(StringBuilder builder) {
    builder.append("InRange[").append(propName).append("]");
  }

  @Override
  public void queryBindKey(BindValuesKey key) {
    key.add(low()).add(high());
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    InRangeExpression that = (InRangeExpression) other;
    return low().equals(that.low()) && high().equals(that.high());
  }
}
