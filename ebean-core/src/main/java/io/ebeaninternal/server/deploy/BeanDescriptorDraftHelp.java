package io.ebeaninternal.server.deploy;

import io.ebean.Query;
import io.ebean.bean.EntityBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper for BeanDescriptor that manages draft entity beans.
 *
 * @param <T> The entity bean type
 */
final class BeanDescriptorDraftHelp<T> {

  private final BeanDescriptor<T> desc;
  private final BeanProperty draftDirty;
  private final BeanProperty[] resetProperties;

  BeanDescriptorDraftHelp(BeanDescriptor<T> desc) {
    this.desc = desc;
    this.draftDirty = desc.getDraftDirty();
    this.resetProperties = resetProperties();
  }

  /**
   * Return the properties that are reset on draft beans after publish.
   */
  private BeanProperty[] resetProperties() {
    List<BeanProperty> list = new ArrayList<>();
    for (BeanProperty prop : desc.propertiesNonMany()) {
      if (prop.isDraftReset()) {
        list.add(prop);
      }
    }
    return list.toArray(new BeanProperty[0]);
  }

  /**
   * Set the value of all the 'reset properties' to null on the draft bean.
   */
  boolean draftReset(T draftBean) {
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
    BeanProperty idProperty = desc.idProperty();
    if (idProperty != null) {
      idProperty.publish(draft, live);
    }
    for (BeanProperty prop : desc.propertiesNonMany()) {
      prop.publish(draft, live);
    }
    for (BeanPropertyAssocMany<?> many : desc.propertiesMany()) {
      if (many.getTargetDescriptor().isDraftable()) {
        many.publishMany(draft, live);
      }
    }
    return liveBean;
  }

  /**
   * Fetch draftable element relationships.
   */
  void draftQueryOptimise(Query<T> query) {
    for (BeanPropertyAssocOne<?> anOne : desc.propertiesOne()) {
      if (anOne.getTargetDescriptor().isDraftableElement()) {
        query.fetch(anOne.name());
      }
    }
    for (BeanPropertyAssocMany<?> aMany : desc.propertiesMany()) {
      if (aMany.getTargetDescriptor().isDraftableElement()) {
        query.fetch(aMany.name());
      }
    }
  }
}
