package io.ebeaninternal.server.cache;

import io.ebean.bean.*;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;

public final class CachedBeanDataToBean {

  public static void load(BeanDescriptor<?> desc, EntityBean bean, CachedBeanData cacheBeanData, PersistenceContext context) {
    EntityBeanIntercept ebi = bean._ebean_getIntercept();
    // any future lazy loading skips L2 bean cache
    ebi.setLoadedFromCache(true);
    BeanProperty idProperty = desc.idProperty();
    if (idProperty != null) {
      // load the id property
      loadProperty(bean, cacheBeanData, ebi, idProperty, context);
    }
    // load the non-many properties
    for (BeanProperty prop : desc.propertiesNonMany()) {
      loadProperty(bean, cacheBeanData, ebi, prop, context);
    }
    final boolean addManyReferences = ebi instanceof InterceptReadWrite;
    for (BeanPropertyAssocMany<?> prop : desc.propertiesMany()) {
      if (prop.isElementCollection()) {
        loadProperty(bean, cacheBeanData, ebi, prop, context);
      } else if (addManyReferences) {
        prop.createReferenceIfNull(bean);
      }
    }
    ebi.setLoadedLazy();
  }

  private static void loadProperty(EntityBean bean, CachedBeanData cacheBeanData, EntityBeanIntercept ebi, BeanProperty prop, PersistenceContext context) {
    if (cacheBeanData.isLoaded(prop.name())) {
      if (!ebi.isLoadedProperty(prop.propertyIndex())) {
        Object value = cacheBeanData.getData(prop.name());
        prop.setCacheDataValue(bean, value, context);
      }
    }
  }

}
