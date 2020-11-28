package io.ebeaninternal.server.deploy.generatedproperty;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.BeanProperty;

/**
 * Used to generate a (Long) timestamp when a bean is inserted.
 */
public class GeneratedInsertLong implements GeneratedProperty {

  /**
   * Return the current time as a Timestamp.
   */
  @Override
  public Object getInsertValue(BeanProperty prop, EntityBean bean, long now) {
    return now;
  }

  /**
   * Just returns the beans original insert timestamp value.
   */
  @Override
  public Object getUpdateValue(BeanProperty prop, EntityBean bean, long now) {
    return prop.getValue(bean);
  }

  /**
   * Return false.
   */
  @Override
  public boolean includeInUpdate() {
    return false;
  }

  @Override
  public boolean includeInAllUpdates() {
    return false;
  }

  /**
   * Return true.
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
