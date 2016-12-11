package io.ebeaninternal.server.persist.dmlbind;

import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;

/**
 * Creates a Bindable to support version concurrency where clauses.
 */
public class FactoryVersion {


  public FactoryVersion() {
  }

  /**
   * Create a Bindable for the version property(s) for a bean type.
   */
  public Bindable create(BeanDescriptor<?> desc) {

    BeanProperty versionProperty = desc.getVersionProperty();
    if (versionProperty == null) {
      return null;
    }

    return new BindableProperty(versionProperty);
  }
}
