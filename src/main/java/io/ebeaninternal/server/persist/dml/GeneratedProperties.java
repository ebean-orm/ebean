package io.ebeaninternal.server.persist.dml;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.generatedproperty.GeneratedProperty;

import java.util.List;

/**
 * Executes the generated property (like WhenCreated, WhoCreated etc) for doc store persisting.
 */
class GeneratedProperties {

  /**
   * Create the GeneratedProperties for the given bean type.
   */
  static GeneratedProperties of(BeanDescriptor<?> desc) {
    return new GeneratedPropertyCollector(desc).generatedProperties();
  }

  private final SetValue[] onInsert;
  private final SetValue[] onUpdate;

  GeneratedProperties(List<BeanProperty> inserts, List<BeanProperty> updates) {
    this.onInsert = adapt(inserts);
    this.onUpdate = adapt(updates);
  }

  private SetValue[] adapt(List<BeanProperty> inserts) {
    SetValue[] setters = new SetValue[inserts.size()];
    for (int i = 0; i < inserts.size(); i++) {
      setters[i] = new SetValue(inserts.get(i));
    }
    return setters;
  }

  /**
   * Set all the generated on insert values.
   */
  public void preInsert(EntityBean bean, long now) {
    for (SetValue setter : onInsert) {
      setter.preInsert(bean, now);
    }
  }

  /**
   * Set all the generated on update values.
   */
  public void preUpdate(EntityBean bean, long now) {
    for (SetValue setter : onUpdate) {
      setter.preUpdate(bean, now);
    }
  }


  private static class SetValue {

    private final BeanProperty property;
    private final GeneratedProperty generatedProperty;

    SetValue(BeanProperty property) {
      this.property = property;
      this.generatedProperty = property.getGeneratedProperty();
    }

    public void preInsert(EntityBean bean, long now) {
      Object value = generatedProperty.getInsertValue(property, bean, now);
      property.setValue(bean, value);
    }

    public void preUpdate(EntityBean bean, long now) {
      Object value = generatedProperty.getUpdateValue(property, bean, now);
      property.setValue(bean, value);
    }
  }
}
