package com.avaje.ebeaninternal.server.cache;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;

public class CachedBeanDataFromBean {

  private final BeanDescriptor<?> desc;
  private final EntityBean bean;
  private final EntityBeanIntercept ebi;

  public static CachedBeanData extract(BeanDescriptor<?> desc, EntityBean bean) {
    return new CachedBeanDataFromBean(desc, bean, bean._ebean_getIntercept()).extract();
  }

  private CachedBeanDataFromBean(BeanDescriptor<?> desc, EntityBean bean, EntityBeanIntercept ebi) {
    this.desc = desc;
    this.bean = bean;
    this.ebi = ebi;
  }

  private CachedBeanData extract() {

    Object[] data = new Object[desc.getPropertyCount()];
    boolean[] loaded = new boolean[desc.getPropertyCount()];
    
    BeanProperty[] props = desc.propertiesNonMany();

    int naturalKeyUpdate = -1;
    for (int i = 0; i < props.length; i++) {
      BeanProperty prop = props[i];
      if (isLoaded(prop)) {
        int propertyIndex = prop.getPropertyIndex();
        data[propertyIndex] = prop.getCacheDataValue(bean);
        loaded[propertyIndex] = true;
        if (prop.isNaturalKey()) {
          naturalKeyUpdate = propertyIndex;
        }
      }
    }

    EntityBean sharableBean = createSharableBean();

    return new CachedBeanData(sharableBean, loaded, data, naturalKeyUpdate);
  }

  private EntityBean createSharableBean() {
    if (!desc.isCacheSharableBeans() || !ebi.isFullyLoadedBean()) {
      return null;
    }
    if (ebi.isReadOnly()) {
      return bean;
    } 
    
    // create a readOnly sharable instance by copying the data
    EntityBean sharableBean = desc.createBean();
    BeanProperty[] propertiesId = desc.propertiesId();
    for (int i = 0; i < propertiesId.length; i++) {
      Object v = propertiesId[i].getValue(bean);
      propertiesId[i].setValue(sharableBean, v);
    }
    BeanProperty[] propertiesNonTransient = desc.propertiesNonTransient();
    for (int i = 0; i < propertiesNonTransient.length; i++) {
      Object v = propertiesNonTransient[i].getValue(bean);
      propertiesNonTransient[i].setValue(sharableBean, v);
    }
    EntityBeanIntercept ebi = ((EntityBean) sharableBean)._ebean_intercept();
    ebi.setReadOnly(true);
    ebi.setLoaded();
    return sharableBean;
  }

  private boolean isLoaded(BeanProperty prop) {
    return ebi.isLoadedProperty(prop.getPropertyIndex());
  }

}