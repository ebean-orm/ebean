package io.ebeaninternal.server.expression;

import io.ebean.bean.EntityBean;
import io.ebean.plugin.ExpressionPath;
import io.ebeaninternal.api.BindValuesKey;
import io.ebeaninternal.api.NaturalKeyQueryData;
import io.ebeaninternal.api.SpiExpression;
import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.server.el.ElPropertyValue;

public final class SimpleExpression extends AbstractValueExpression {

  private final Op type;

  public SimpleExpression(String propertyName, Op type, Object value) {
    super(propertyName, value);
    this.type = type;
  }

  @Override
  public Object getIdEqualTo(String idName) {
    if (type == Op.EQ && idName.equals(propName)) {
      return value();
    }
    return null;
  }

  @Override
  public boolean naturalKey(NaturalKeyQueryData<?> data) {
    // can't use naturalKey cache for NOT IN
    if (type != Op.EQ) {
      return false;
    }
    return data.matchEq(propName, bindValue);
  }

  public String getPropName() {
    return propName;
  }

  public boolean isOpEquals() {
    return Op.EQ == type;
  }

  public Object getValue() {
    return value();
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {

    ElPropertyValue prop = getElProp(request);
    if (prop != null) {
      if (prop.isAssocId()) {
        Object[] ids = prop.assocIdValues((EntityBean) value());
        if (ids != null) {
          for (Object id : ids) {
            request.addBindValue(id);
          }
        }
        return;
      }
      if (prop.isDbEncrypted()) {
        // bind the key as well as the value
        String encryptKey = prop.beanProperty().encryptKey().getStringValue();
        request.addBindEncryptKey(encryptKey);
      } else if (prop.isLocalEncrypted()) {
        Object bindVal = prop.localEncrypt(value());
        request.addBindEncryptKey(bindVal);
        return;
      }
    }

    request.addBindValue(value());
  }

  @Override
  public void addSql(SpiExpressionRequest request) {
    ElPropertyValue prop = getElProp(request);
    if (prop != null) {
      if (prop.isAssocId()) {
        request.parse(prop.assocIdExpression(propName, type.bind()));
        return;
      }
      if (prop.isDbEncrypted()) {
        String dsql = prop.beanProperty().decryptProperty(propName);
        request.parse(dsql).append(type.bind());
        return;
      }
    }
    request.property(propName).append(type.bind());
  }

  /**
   * Based on the type and propertyName.
   */
  @Override
  public void queryPlanHash(StringBuilder builder) {
    builder.append(type.name()).append('[').append(propName).append(']');
  }

  @Override
  public void queryBindKey(BindValuesKey key) {
    key.add(value());
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    SimpleExpression that = (SimpleExpression) other;
    return value().equals(that.value());
  }
}
