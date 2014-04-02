package com.avaje.ebeaninternal.server.cache;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;

public class CachedBeanDataToBean {

  private final BeanDescriptor<?> desc;
  private final EntityBean bean;
  private final EntityBeanIntercept ebi;
  private final CachedBeanData cacheBeanData;
  //private final boolean readOnly;

  public static boolean load(BeanDescriptor<?> desc, EntityBean bean, CachedBeanData cacheBeandata) {
    return new CachedBeanDataToBean(desc, bean, ((EntityBean) bean)._ebean_getIntercept(), cacheBeandata).load();
  }

  private CachedBeanDataToBean(BeanDescriptor<?> desc, EntityBean bean, EntityBeanIntercept ebi, CachedBeanData cacheBeandata) {
    this.desc = desc;
    this.bean = bean;
    this.ebi = ebi;
    this.cacheBeanData = cacheBeandata;
    //this.readOnly = ebi.isReadOnly();
  }

  private boolean load() {

    BeanProperty[] props = desc.propertiesNonMany();
    for (int i = 0; i < props.length; i++) {

      BeanProperty prop = props[i];
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

    BeanPropertyAssocMany<?>[] manys = desc.propertiesMany();
    for (int i = 0; i < manys.length; i++) {
      BeanPropertyAssocMany<?> prop = manys[i];
      if (ebi.isLoadedProperty(prop.getPropertyIndex())) {
        // already loaded property
      } else {
        // set a lazy loading proxy
        prop.createReference(bean);
      }
    }

    ebi.setLoadedLazy();

    return true;
  }

}