package io.ebeaninternal.server.expression;

import io.ebean.bean.EntityBean;
import io.ebean.plugin.ExpressionPath;
import io.ebeaninternal.api.SpiExpression;
import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.server.el.ElPropertyValue;
import io.ebeaninternal.api.NaturalKeyQueryData;

import java.io.IOException;
import java.util.Arrays;

public class SimpleExpression extends AbstractValueExpression {

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

  @Override
  public void writeDocQuery(DocQueryContext context) throws IOException {
    if (type == Op.BETWEEN) {
      throw new IllegalStateException("BETWEEN Not expected in SimpleExpression?");
    }
    ExpressionPath prop = context.getExpressionPath(propName);
    if (prop != null && prop.isAssocId()) {
      String idName = prop.getAssocIdExpression(propName, "");
      Object[] ids = prop.getAssocIdValues((EntityBean) value());
      if (ids == null || ids.length != 1) {
        throw new IllegalArgumentException("Expecting 1 Id value for " + idName + " but got " + Arrays.toString(ids));
      }
      context.writeSimple(type, idName, ids[0]);
    } else {
      context.writeSimple(type, propName, value());
    }
  }

  public final String getPropName() {
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
        Object[] ids = prop.getAssocIdValues((EntityBean) value());
        if (ids != null) {
          for (Object id : ids) {
            request.addBindValue(id);
          }
        }
        return;
      }
      if (prop.isDbEncrypted()) {
        // bind the key as well as the value
        String encryptKey = prop.getBeanProperty().getEncryptKey().getStringValue();
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
        request.append(prop.getAssocIdExpression(propName, type.bind()));
        return;
      }
      if (prop.isDbEncrypted()) {
        String dsql = prop.getBeanProperty().getDecryptSql();
        request.append(dsql).append(type.bind());
        return;
      }
    }
    request.append(propName).append(type.bind());
  }

  /**
   * Based on the type and propertyName.
   */
  @Override
  public void queryPlanHash(StringBuilder builder) {
    builder.append(type.name()).append("[").append(propName).append("]");
  }

  @Override
  public int queryBindHash() {
    return value().hashCode();
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    SimpleExpression that = (SimpleExpression) other;
    return value().equals(that.value());
  }
}
