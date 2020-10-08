package io.ebeaninternal.server.deploy.generatedproperty;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.BeanProperty;

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
    return 1L;
  }

  /**
   * Increments the current value by one.
   */
  @Override
  public Object getUpdateValue(BeanProperty prop, EntityBean bean, long now) {
    Long val = (Long) prop.getValue(bean);
    if (val == null) {
      throw new IllegalStateException("version property has been set to null on bean: " + bean);
    }
    return val + 1;
  }

  /**
   * Include this in every update.
   */
  @Override
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
  @Override
  public boolean includeInInsert() {
    return true;
  }

  @Override
  public boolean isDDLNotNullable() {
    return true;
  }

}
