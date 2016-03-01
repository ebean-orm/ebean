package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;

import java.io.IOException;

public class SimpleExpression extends AbstractExpression {

  private static final long serialVersionUID = -382881395755603790L;

  private final Op type;

  private final Object value;

  public SimpleExpression(String propertyName, Op type, Object value) {
    super(propertyName);
    this.type = type;
    this.value = value;
  }

  @Override
  public void writeElastic(ElasticExpressionContext context) throws IOException {

    if (type == Op.BETWEEN) {
      throw new IllegalStateException("BETWEEN Not expected in SimpleExpression?");
    }

    context.writeSimple(type, propName, value);
  }

  public final String getPropName() {
    return propName;
  }

  public boolean isOpEquals() {
    return Op.EQ.equals(type);
  }

  public Object getValue() {
    return value;
  }

  @Override
  public void addBindValues(SpiExpressionRequest request) {

    ElPropertyValue prop = getElProp(request);
    if (prop != null) {
      if (prop.isAssocId()) {
        Object[] ids = prop.getAssocOneIdValues((EntityBean) value);
        if (ids != null) {
          for (int i = 0; i < ids.length; i++) {
            request.addBindValue(ids[i]);
          }
        }
        return;
      }
      if (prop.isDbEncrypted()) {
        // bind the key as well as the value
        String encryptKey = prop.getBeanProperty().getEncryptKey().getStringValue();
        request.addBindEncryptKey(encryptKey);
      }
      //else if (prop.isLocalEncrypted()) {
      // not supporting this for equals (but probably could)
      // prop.getBeanProperty().getScalarType();
    }

    request.addBindValue(value);
  }

  @Override
  public void addSql(SpiExpressionRequest request) {

    ElPropertyValue prop = getElProp(request);
    if (prop != null) {
      if (prop.isAssocId()) {
        request.append(prop.getAssocOneIdExpr(propName, type.bind()));
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
  public void queryPlanHash(HashQueryPlanBuilder builder) {
    builder.add(SimpleExpression.class).add(propName).add(type.name());
    builder.bind(1);
  }

  @Override
  public int queryBindHash() {
    return value.hashCode();
  }

  @Override
  public boolean isSameByPlan(SpiExpression other) {
    if (!(other instanceof SimpleExpression)) {
      return false;
    }

    SimpleExpression that = (SimpleExpression) other;
    return this.propName.equals(that.propName)
        && this.type == that.type;
  }

  @Override
  public boolean isSameByBind(SpiExpression other) {
    SimpleExpression that = (SimpleExpression) other;
    return value.equals(that.value);
  }
}
