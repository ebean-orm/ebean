package com.avaje.ebeaninternal.server.type;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.text.StringParser;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;

/**
 * Adapter for CtCompoundProperty to ElPropertyValue.
 * <p>
 * This is used for non-scalar properties of a Compound Value Object. These only
 * occur in nested compound types.
 * </p>
 *
 * @author rbygrave
 */
public class CtCompoundPropertyElAdapter implements ElPropertyValue {

  private final CtCompoundProperty prop;

  private int deployOrder;

  public CtCompoundPropertyElAdapter(CtCompoundProperty prop) {
    this.prop = prop;
  }

  public void setDeployOrder(int deployOrder) {
    this.deployOrder = deployOrder;
  }

  @Override
  public Object convert(Object value) {
    return value;
  }

  @Override
  public Object pathGetNested(Object bean) {
    return bean;
  }

  @Override
  public Object pathGet(Object bean) {
    return prop.getValue(bean);
  }

  @Override
  public void pathSet(Object bean, Object value) {
    prop.setValue(bean, value);
  }

  public String getAssocIdExpression(String prefix, String operator) {
    throw new RuntimeException("Not Supported or Expected");
  }

  public Object[] getAssocIdValues(EntityBean bean) {
    throw new RuntimeException("Not Supported or Expected");
  }

  public String getAssocIdInExpr(String prefix) {
    throw new RuntimeException("Not Supported or Expected");
  }

  public String getAssocIdInValueExpr(int size) {
    throw new RuntimeException("Not Supported or Expected");
  }

  public BeanProperty getBeanProperty() {
    return null;
  }

  public StringParser getStringParser() {
    return null;
  }

  public boolean isDbEncrypted() {
    return false;
  }

  public boolean isLocalEncrypted() {
    return false;
  }

  public boolean isAssocId() {
    return false;
  }

  public boolean isAssocProperty() {
    return false;
  }

  public boolean isDateTimeCapable() {
    return false;
  }

  public int getJdbcType() {
    return 0;
  }

  public Object parseDateTime(long systemTimeMillis) {
    throw new RuntimeException("Not Supported or Expected");
  }

  @Override
  public boolean containsFormulaWithJoin() {
    return false;
  }

  public boolean containsMany() {
    return false;
  }

  public boolean containsManySince(String sinceProperty) {
    return containsMany();
  }

  public String getDbColumn() {
    return null;
  }

  public String getElPlaceholder(boolean encrypted) {
    return null;
  }

  public String getElPrefix() {
    return null;
  }

  public String getName() {
    return prop.getPropertyName();
  }

  public String getElName() {
    return prop.getPropertyName();
  }

}
