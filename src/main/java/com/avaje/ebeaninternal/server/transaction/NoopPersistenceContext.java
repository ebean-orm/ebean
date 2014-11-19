package com.avaje.ebeaninternal.server.transaction;

import com.avaje.ebean.bean.PersistenceContext;

/**
 * PersistenceContext used with scope of NONE.
 * <p/>
 * When used effectively means no PersistenceContext is used at all. This is not expected to be used much and
 * actually is not recommended.
 */
public class NoopPersistenceContext implements PersistenceContext {

  @Override
  public void put(Object id, Object bean) {
    // do nothing
  }

  @Override
  public Object putIfAbsent(Object id, Object bean) {
    // do nothing
    return null;
  }

  @Override
  public Object get(Class<?> beanType, Object uid) {
    // do nothing, return null
    return null;
  }

  @Override
  public WithOption getWithOption(Class<?> beanType, Object uid) {
    // do nothing, return null
    return null;
  }

  @Override
  public void clear() {
    // do nothing
  }

  @Override
  public void clear(Class<?> beanType) {
    // do nothing
  }

  @Override
  public void clear(Class<?> beanType, Object uid) {
    // do nothing
  }

  @Override
  public void deleted(Class<?> beanType, Object id) {
    // do nothing
  }

  @Override
  public int size(Class<?> beanType) {
    return 0;
  }
}
