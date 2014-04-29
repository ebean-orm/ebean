package com.avaje.ebeaninternal.server.cache;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;

public class CachedBeanDataToBean {


  public static boolean load(BeanDescriptor<?> desc, EntityBean bean, CachedBeanData cacheBeanData) {
    
    EntityBeanIntercept ebi = bean._ebean_getIntercept();

    
    BeanProperty idProperty = desc.getIdProperty();
    if (idProperty != null) {
      // load the id property
      loadProperty(bean, cacheBeanData, ebi, idProperty);
    }
    
    // load the non-many properties
    BeanProperty[] props = desc.propertiesNonMany();
    for (int i = 0; i < props.length; i++) {
      loadProperty(bean, cacheBeanData, ebi, props[i]);
    }

    BeanPropertyAssocMany<?>[] manys = desc.propertiesMany();
    for (int i = 0; i < manys.length; i++) {
      manys[i].createReferenceIfNull(bean);
    }

    ebi.setLoadedLazy();

    return true;
  }

  private static void loadProperty(EntityBean bean, CachedBeanData cacheBeanData, EntityBeanIntercept ebi, BeanProperty prop) {
   
    int propertyIndex = prop.getPropertyIndex();
    if (cacheBeanData.isLoaded(propertyIndex)) {
      if (ebi.isLoadedProperty(propertyIndex)) {
        // already loaded (lazy load on partially loaded bean)
      } else {
        Object data = cacheBeanData.getData(propertyIndex);
        prop.setCacheDataValue(bean, data);
      }
    }
  }

}