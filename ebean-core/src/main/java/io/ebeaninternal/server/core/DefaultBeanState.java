package io.ebeaninternal.server.core;

import io.ebean.BeanState;
import io.ebean.ValuePair;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebean.bean.InterceptReadOnly;

import java.util.Map;
import java.util.Set;

/**
 * Default implementation of BeanState.
 */
public final class DefaultBeanState implements BeanState {

  private final EntityBeanIntercept intercept;

  public DefaultBeanState(EntityBean entityBean) {
    this.intercept = entityBean._ebean_getIntercept();
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
    return intercept.isNewOrDirty();
  }

  @Override
  public boolean isDirty() {
    return intercept.isDirty();
  }

  @Override
  public Set<String> loadedProps() {
    return intercept.loadedPropertyNames();
  }

  @Override
  public Set<String> changedProps() {
    return intercept.dirtyPropertyNames();
  }

  @Override
  public Map<String, ValuePair> dirtyValues() {
    return intercept.dirtyValues();
  }

  @Override
  public boolean isUnmodifiable() {
    return intercept instanceof InterceptReadOnly;
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

  @Override
  public Map<String, Exception> loadErrors() {
    return intercept.loadErrors();
  }

  @Override
  public int sortOrder() {
    return intercept.sortOrder();
  }
}
