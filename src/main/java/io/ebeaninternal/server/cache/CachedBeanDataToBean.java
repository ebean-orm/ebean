package io.ebeaninternal.server.cache;

import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebean.bean.PersistenceContext;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;

public class CachedBeanDataToBean {


  public static void load(BeanDescriptor<?> desc, EntityBean bean, CachedBeanData cacheBeanData, PersistenceContext context) {

    EntityBeanIntercept ebi = bean._ebean_getIntercept();

    BeanProperty idProperty = desc.getIdProperty();
    if (desc.getInheritInfo() != null) {
        desc = desc.getInheritInfo().readType(bean.getClass()).desc();
    }

    if (idProperty != null) {
      // load the id property
      loadProperty(bean, cacheBeanData, ebi, idProperty, context);
    }

    // load the non-many properties
    for (BeanProperty prop : desc.propertiesNonMany()) {
      loadProperty(bean, cacheBeanData, ebi, prop, context);
    }

    for (BeanPropertyAssocMany<?> prop : desc.propertiesMany()) {
      if (prop.isElementCollection()) {
        loadProperty(bean, cacheBeanData, ebi, prop, context);
      } else {
        prop.createReferenceIfNull(bean);
      }
    }

    ebi.setLoadedLazy();
  }

  private static void loadProperty(EntityBean bean, CachedBeanData cacheBeanData, EntityBeanIntercept ebi, BeanProperty prop, PersistenceContext context) {

    if (cacheBeanData.isLoaded(prop.getName())) {
      if (!ebi.isLoadedProperty(prop.getPropertyIndex())) {
        Object value = cacheBeanData.getData(prop.getName());
        prop.setCacheDataValue(bean, value, context);
      }
    }
  }

}
