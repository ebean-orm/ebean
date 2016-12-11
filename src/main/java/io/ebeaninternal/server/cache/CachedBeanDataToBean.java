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
    if (idProperty != null) {
      // load the id property
      loadProperty(bean, cacheBeanData, ebi, idProperty, context);
    }

    // load the non-many properties
    BeanProperty[] props = desc.propertiesNonMany();
    for (BeanProperty prop : props) {
      loadProperty(bean, cacheBeanData, ebi, prop, context);
    }

    BeanPropertyAssocMany<?>[] many = desc.propertiesMany();
    for (BeanPropertyAssocMany<?> aMany : many) {
      aMany.createReferenceIfNull(bean);
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
