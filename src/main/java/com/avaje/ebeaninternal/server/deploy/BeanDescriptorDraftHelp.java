package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.bean.EntityBean;

/**
 * Helper for BeanDescriptor that manages draft entity beans.
 * 
 * @param <T> The entity bean type
 */
public final class BeanDescriptorDraftHelp<T> {

  private final BeanDescriptor<T> desc;

  public BeanDescriptorDraftHelp(BeanDescriptor<T> desc) {
    this.desc = desc;
  }

  /**
   *
   */
  public void initialise() {
  }

  public T publish(T draftBean, T liveBean) {

    if (liveBean == null) {
      liveBean = (T)desc.createEntityBean();
    }

    EntityBean draft = (EntityBean)draftBean;
    EntityBean live = (EntityBean)liveBean;

    BeanProperty idProperty = desc.getIdProperty();
    if (idProperty != null) {
      idProperty.publish(draft, live);
    }

    BeanProperty[] props = desc.propertiesNonMany();
    for (int i = 0; i < props.length; i++) {
      props[i].publish(draft, live);
    }

    BeanPropertyAssocMany<?>[] many = desc.propertiesMany();
    for (int i = 0; i < many.length; i++) {
      if (many[i].getTargetDescriptor().isDraftable()) {
        many[i].publishMany(draft, live);
      }
    }

    return liveBean;
  }
}
