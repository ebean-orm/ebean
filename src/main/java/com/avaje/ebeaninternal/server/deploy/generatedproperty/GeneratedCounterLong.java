package com.avaje.ebeaninternal.server.deploy.generatedproperty;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;

/**
 * Used to create a counter version column for Long.
 */
public class GeneratedCounterLong implements GeneratedProperty {

  public GeneratedCounterLong() {

  }

  /**
   * Always returns a 1.
   */
  @Override
  public Object getInsertValue(BeanProperty prop, EntityBean bean, long now) {
    return (long) 1;
  }

  /**
   * Increments the current value by one.
   */
  @Override
  public Object getUpdateValue(BeanProperty prop, EntityBean bean, long now) {
    Long i = (Long) prop.getValue(bean);
    return i + 1;
  }

  /**
   * Include this in every update.
   */
  public boolean includeInUpdate() {
    return true;
  }

  @Override
  public boolean includeInAllUpdates() {
    return false;
  }

  /**
   * Include this in every insert setting initial counter value to 1.
   */
  public boolean includeInInsert() {
    return true;
  }

  public boolean isDDLNotNullable() {
    return true;
  }

}
