package com.avaje.ebeaninternal.server.cache;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;

public class CachedBeanDataFromBean {


  public static CachedBeanData extract(BeanDescriptor<?> desc, EntityBean bean) {

    EntityBeanIntercept ebi = bean._ebean_getIntercept();
    
    Object[] data = new Object[desc.getPropertyCount()];
    boolean[] loaded = new boolean[desc.getPropertyCount()];
    
    BeanProperty[] props = desc.propertiesNonMany();

    Object naturalKey = null;

    for (int i = 0; i < props.length; i++) {
      BeanProperty prop = props[i];
      if (ebi.isLoadedProperty(prop.getPropertyIndex())) {
        int propertyIndex = prop.getPropertyIndex();
        data[propertyIndex] = prop.getCacheDataValue(bean);
        loaded[propertyIndex] = true;
        if (prop.isNaturalKey()) {
          naturalKey = prop.getValue(bean);
        }
      }
    }

    EntityBean sharableBean = createSharableBean(desc, bean, ebi);

    return new CachedBeanData(sharableBean, loaded, data, naturalKey, null);
  }

  private static EntityBean createSharableBean(BeanDescriptor<?> desc, EntityBean bean, EntityBeanIntercept beanEbi) {
    
    if (!desc.isCacheSharableBeans() || !beanEbi.isFullyLoadedBean()) {
      return null;
    }
    if (beanEbi.isReadOnly()) {
      return bean;
    } 
    
    // create a readOnly sharable instance by copying the data
    EntityBean sharableBean = desc.createBean();
    BeanProperty idProp = desc.getIdProperty();
    if (idProp != null) {
      Object v = idProp.getValue(bean);
      idProp.setValue(sharableBean, v);
    }
    BeanProperty[] propertiesNonTransient = desc.propertiesNonTransient();
    for (int i = 0; i < propertiesNonTransient.length; i++) {
      Object v = propertiesNonTransient[i].getValue(bean);
      propertiesNonTransient[i].setValue(sharableBean, v);
    }
    EntityBeanIntercept intercept = sharableBean._ebean_intercept();
    intercept.setReadOnly(true);
    intercept.setLoaded();
    return sharableBean;
  }


}