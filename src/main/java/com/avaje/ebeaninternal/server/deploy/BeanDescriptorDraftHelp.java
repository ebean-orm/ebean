package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.Query;
import com.avaje.ebean.bean.EntityBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper for BeanDescriptor that manages draft entity beans.
 *
 * @param <T> The entity bean type
 */
public final class BeanDescriptorDraftHelp<T> {

  private final BeanDescriptor<T> desc;

  private final BeanProperty draftDirty;

  private final BeanProperty[] resetProperties;

  public BeanDescriptorDraftHelp(BeanDescriptor<T> desc) {
    this.desc = desc;
    this.draftDirty = desc.getDraftDirty();
    this.resetProperties = resetProperties();
  }

  /**
   * Return the properties that are reset on draft beans after publish.
   */
  private BeanProperty[] resetProperties() {

    List<BeanProperty> list = new ArrayList<>();

    BeanProperty[] props = desc.propertiesNonMany();
    for (BeanProperty prop : props) {
      if (prop.isDraftReset()) {
        list.add(prop);
      }
    }

    return list.toArray(new BeanProperty[list.size()]);
  }

  /**
   * Set the value of all the 'reset properties' to null on the draft bean.
   */
  public boolean draftReset(T draftBean) {

    EntityBean draftEntityBean = (EntityBean) draftBean;

    if (draftDirty != null) {
      // set @DraftDirty property to false
      draftDirty.setValueIntercept(draftEntityBean, false);
    }

    // set to null on all @DraftReset properties
    for (BeanProperty resetProperty : resetProperties) {
      resetProperty.setValueIntercept(draftEntityBean, null);
    }

    // return true if the bean is dirty (and should be persisted)
    return draftEntityBean._ebean_getIntercept().isDirty();
  }

  /**
   * Transfer the values from the draftBean to the liveBean.
   * <p>
   * This will recursive transfer values to all @DraftableElement properties.
   * </p>
   */
  @SuppressWarnings("unchecked")
  public T publish(T draftBean, T liveBean) {

    if (liveBean == null) {
      liveBean = (T) desc.createEntityBean();
    }

    EntityBean draft = (EntityBean) draftBean;
    EntityBean live = (EntityBean) liveBean;

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

  /**
   * Fetch draftable element relationships.
   */
  public void draftQueryOptimise(Query<T> query) {

    BeanPropertyAssocOne<?>[] one = desc.propertiesOne();
    for (int i = 0; i < one.length; i++) {
      if (one[i].getTargetDescriptor().isDraftableElement()) {
        query.fetch(one[i].getName());
      }
    }

    BeanPropertyAssocMany<?>[] many = desc.propertiesMany();
    for (int i = 0; i < many.length; i++) {
      if (many[i].getTargetDescriptor().isDraftableElement()) {
        query.fetch(many[i].getName());
      }
    }

  }
}
