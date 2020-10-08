package io.ebeaninternal.server.deploy.generatedproperty;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.BeanProperty;

/**
 * Generate a (Long) Timestamp whenever the bean is inserted or updated.
 */
public class GeneratedUpdateLong implements GeneratedProperty {

  /**
   * Return now as a Timestamp.
   */
  @Override
  public Object getInsertValue(BeanProperty prop, EntityBean bean, long now) {
    return now;
  }

  /**
   * Return now as a Timestamp.
   */
  @Override
  public Object getUpdateValue(BeanProperty prop, EntityBean bean, long now) {
    return now;
  }

  /**
   * For dynamic table updates make sure this is included.
   */
  @Override
  public boolean includeInUpdate() {
    return true;
  }

  @Override
  public boolean includeInAllUpdates() {
    return true;
  }

  /**
   * Include this in every insert.
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
