package io.ebeaninternal.server.core;

import io.ebean.BeanState;
import io.ebean.ValuePair;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebeaninternal.server.deploy.BeanDescriptor;

import java.util.Map;
import java.util.Set;

/**
 * Default implementation of BeanState.
 */
public class DefaultBeanState implements BeanState {

  private final EntityBeanIntercept intercept;

  private final BeanDescriptor<?> descriptor;


  public DefaultBeanState(EntityBean entityBean, BeanDescriptor<?> beanType) {
    this.intercept = entityBean._ebean_getIntercept();
    this.descriptor = beanType;
  }

  @Override
  public void setPropertyLoaded(String propertyName, boolean loaded) {
    intercept.setPropertyLoaded(propertyName, loaded);
  }

  @Override
  public boolean isReference() {
    return intercept.isReference();
  }

  @Override
  public boolean isNew() {
    return intercept.isNew();
  }

  @Override
  public boolean isNewOrDirty() {
    if (!intercept.isNew()) {
      descriptor.checkMutableProperties(intercept);
    }
    return intercept.isNewOrDirty();
  }

  @Override
  public boolean isDirty() {
    if (!intercept.isNew()) {
      descriptor.checkMutableProperties(intercept);
    }
    return intercept.isDirty();
  }

  @Override
  public Set<String> getLoadedProps() {
    return intercept.getLoadedPropertyNames();
  }

  @Override
  public Set<String> getChangedProps() {
    return intercept.getDirtyPropertyNames();
  }

  @Override
  public Map<String, ValuePair> getDirtyValues() {
    if (!intercept.isNew()) {
      descriptor.checkMutableProperties(intercept);
    }
    return intercept.getDirtyValues();
  }

  @Override
  public boolean isReadOnly() {
    return intercept.isReadOnly();
  }

  @Override
  public void setReadOnly(boolean readOnly) {
    intercept.setReadOnly(readOnly);
  }

  @Override
  public void setLoaded() {
    intercept.setLoaded();
    descriptor.setMutableOrigValues(intercept);
  }

  @Override
  public void setDisableLazyLoad(boolean disableLazyLoading) {
    intercept.setDisableLazyLoad(disableLazyLoading);
  }

  @Override
  public boolean isDisableLazyLoad() {
    return intercept.isDisableLazyLoad();
  }

  @Override
  public void resetForInsert() {
    intercept.setNew();
  }
}
