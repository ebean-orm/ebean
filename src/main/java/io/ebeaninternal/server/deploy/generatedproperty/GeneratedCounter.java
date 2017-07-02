package io.ebeaninternal.server.deploy.generatedproperty;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.core.BasicTypeConverter;
import io.ebeaninternal.server.deploy.BeanProperty;

/**
 * A general number counter for various number types.
 */
public class GeneratedCounter implements GeneratedProperty {

  final int numberType;

  public GeneratedCounter(int numberType) {
    this.numberType = numberType;
  }

  /**
   * Always returns a 1.
   */
  @Override
  public Object getInsertValue(BeanProperty prop, EntityBean bean, long now) {
    return BasicTypeConverter.convert(1, numberType);
  }

  /**
   * Increments the current value by one.
   */
  @Override
  public Object getUpdateValue(BeanProperty prop, EntityBean bean, long now) {
    Number currVal = (Number) prop.getValue(bean);
    if (currVal == null) {
      throw new IllegalStateException("version property has been set to null on bean: " + bean);
    }
    Integer nextVal = currVal.intValue() + 1;
    return BasicTypeConverter.convert(nextVal, numberType);
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
