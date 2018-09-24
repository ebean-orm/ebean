package io.ebeaninternal.server.deploy;

import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;

class ElementEntityBean implements EntityBean {

  private final String[] properties;

  private Object[] data;

  private EntityBeanIntercept intercept;

  ElementEntityBean(String[] properties) {
    this.properties = properties;
    this.intercept = new EntityBeanIntercept(this);
  }

  @Override
  public String[] _ebean_getPropertyNames() {
    return properties;
  }

  @Override
  public String _ebean_getPropertyName(int pos) {
    return properties[pos];
  }

  @Override
  public String _ebean_getMarker() {
    return null;
  }

  @Override
  public Object _ebean_newInstance() {
    return new ElementEntityBean(properties);
  }

  @Override
  public void _ebean_setEmbeddedLoaded() {

  }

  @Override
  public boolean _ebean_isEmbeddedNewOrDirty() {
    return false;
  }

  @Override
  public EntityBeanIntercept _ebean_getIntercept() {
    return intercept;
  }

  @Override
  public EntityBeanIntercept _ebean_intercept() {
    return intercept;
  }

  @Override
  public void _ebean_setField(int fieldIndex, Object value) {
    if (data == null) {
      data = new Object[properties.length];
    }
    data[fieldIndex] = value;
  }

  @Override
  public void _ebean_setFieldIntercept(int fieldIndex, Object value) {
    _ebean_setField(fieldIndex, value);
  }

  @Override
  public Object _ebean_getField(int fieldIndex) {
    return data == null ? null : data[fieldIndex];
  }

  @Override
  public Object _ebean_getFieldIntercept(int fieldIndex) {
    return _ebean_getField(fieldIndex);
  }
}
