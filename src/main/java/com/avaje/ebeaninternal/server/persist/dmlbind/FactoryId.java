package com.avaje.ebeaninternal.server.persist.dmlbind;

import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocOne;

/**
 * Create a Bindable for the ids of a bean type.
 */
public class FactoryId {

  public FactoryId() {
  }

  /**
   * Add uniqueId properties.
   */
  public BindableId createId(BeanDescriptor<?> desc) {

    BeanProperty id = desc.getIdProperty();
    if (id == null) {
      return new BindableIdEmpty();

    }     
    if (!id.isEmbedded()) {
      return new BindableIdScalar(id);

    } else {
      BeanPropertyAssocOne<?> embId = (BeanPropertyAssocOne<?>) id;
      return new BindableIdEmbedded(embId, desc);
    }
  }
}
