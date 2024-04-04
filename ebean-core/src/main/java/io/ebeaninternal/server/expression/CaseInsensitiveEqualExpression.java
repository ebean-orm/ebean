package io.ebeaninternal.server.expression;

import io.ebeaninternal.api.BindValuesKey;
import io.ebeaninternal.api.SpiExpression;
import io.ebeaninternal.api.SpiExpressionBind;
import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.server.el.ElPropertyValue;

final class CaseInsensitiveEqualExpression extends AbstractValueExpression {

  private final boolean not;

  CaseInsensitiveEqualExpression(String propertyName, Object value, boolean not) {
    super(propertyName, value);
    this.not = not;
  }

  /**
   * Return the bind value taking into account named parameters.
   */
  private String val() {
    return strValue().toLowerCase();
  }

  @Override
  public void addBindValues(SpiExpressionBind request) {
    ElPropertyValue prop = getElProp(request);
    if (prop != null && prop.isDbEncrypted()) {
      // bind the key as well as the value
      String encryptKey = prop.beanProperty().encryptKey().getStringValue();
      request.addBindEncryptKey(encryptKey);
    }

    request.addBindValue(val());
  }

  @Override
  public void addSql(SpiExpressionRequest request) {
    String pname = propName;
    ElPropertyValue prop = getElProp(request);
    if (prop != null && prop.isDbEncrypted()) {
      pname = prop.beanProperty().decryptProperty(propName);
    }
    if (not) {
      request.append("lower(").property(pname).append(") != ?");
    } else {
      request.append("lower(").property(pname).append(") = ?");
    }
  }

  @Override
  public void queryPlanHash(StringBuilder builder) {
    if (not) {
      builder.append("Ine[").append(propName).append(']');
    } else {
      builder.append("Ieq[").append(propName).append(']');
    }
  }

  @Override
  public void queryBindKey(BindValuesKey key) {
    key.add(val());
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    CaseInsensitiveEqualExpression that = (CaseInsensitiveEqualExpression) other;
    return val().equals(that.val());
  }
}
