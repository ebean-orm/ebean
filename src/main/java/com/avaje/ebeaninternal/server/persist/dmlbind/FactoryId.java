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

    BeanProperty[] uids = desc.propertiesId();
    if (uids.length == 0) {
      return new BindableIdEmpty();

    } else if (uids.length == 1) {
      if (!uids[0].isEmbedded()) {
        return new BindableIdScalar(uids[0]);

      } else {
        BeanPropertyAssocOne<?> embId = (BeanPropertyAssocOne<?>) uids[0];
        return new BindableIdEmbedded(embId, desc);
      }
    } else {
      return new BindableIdMap(uids, desc);
    }
  }
}
