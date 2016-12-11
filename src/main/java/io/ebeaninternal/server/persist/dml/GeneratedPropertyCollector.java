package io.ebeaninternal.server.persist.dml;

import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.generatedproperty.GeneratedProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects the generated properties for inserts and updates for a given bean type.
 */
class GeneratedPropertyCollector {

  private final List<BeanProperty> preInsert = new ArrayList<>();
  private final List<BeanProperty> preUpdate = new ArrayList<>();

  GeneratedPropertyCollector(BeanDescriptor<?> desc) {
    for (BeanProperty beanProperty : desc.propertiesBaseScalar()) {
      add(beanProperty);
    }
  }

  GeneratedProperties generatedProperties() {
    return new GeneratedProperties(preInsert, preUpdate);
  }

  void add(BeanProperty prop) {
    GeneratedProperty gen = prop.getGeneratedProperty();
    if (gen != null) {
      if (gen.includeInInsert()) {
        preInsert.add(prop);
      }
      if (gen.includeInUpdate()) {
        preUpdate.add(prop);
      }
    }
  }

}
