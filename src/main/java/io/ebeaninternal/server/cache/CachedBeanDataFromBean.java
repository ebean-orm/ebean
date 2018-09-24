package io.ebeaninternal.server.cache;

import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;

import java.util.LinkedHashMap;
import java.util.Map;

public class CachedBeanDataFromBean {


  public static CachedBeanData extract(BeanDescriptor<?> desc, EntityBean bean) {

    EntityBeanIntercept ebi = bean._ebean_getIntercept();

    Map<String, Object> data = new LinkedHashMap<>();

    BeanProperty idProperty = desc.getIdProperty();
    if (idProperty != null) {
      int propertyIndex = idProperty.getPropertyIndex();
      if (ebi.isLoadedProperty(propertyIndex)) {
        data.put(idProperty.getName(), idProperty.getCacheDataValue(bean));
      }
    }
    BeanProperty[] props = desc.propertiesNonMany();

    // extract all the non-many properties
    for (BeanProperty prop : props) {
      if (ebi.isLoadedProperty(prop.getPropertyIndex())) {
        data.put(prop.getName(), prop.getCacheDataValue(bean));
      }
    }

    for (BeanPropertyAssocMany<?> prop : desc.propertiesMany()) {
      if (prop.isElementCollection()) {
        data.put(prop.getName(), prop.getCacheDataValue(bean));
      }
    }

    long version = desc.getVersion(bean);
    EntityBean sharableBean = createSharableBean(desc, bean, ebi);
    return new CachedBeanData(sharableBean, desc.getDiscValue(), data, version);
  }

  private static EntityBean createSharableBean(BeanDescriptor<?> desc, EntityBean bean, EntityBeanIntercept beanEbi) {

    if (!desc.isCacheSharableBeans() || !beanEbi.isFullyLoadedBean()) {
      return null;
    }
    if (beanEbi.isReadOnly()) {
      return bean;
    }

    // create a readOnly sharable instance by copying the data
    EntityBean sharableBean = desc.createEntityBean();
    BeanProperty idProp = desc.getIdProperty();
    if (idProp != null) {
      Object v = idProp.getValue(bean);
      idProp.setValue(sharableBean, v);
    }
    BeanProperty[] propertiesNonTransient = desc.propertiesNonTransient();
    for (BeanProperty aPropertiesNonTransient : propertiesNonTransient) {
      Object v = aPropertiesNonTransient.getValue(bean);
      aPropertiesNonTransient.setValue(sharableBean, v);
    }
    EntityBeanIntercept intercept = sharableBean._ebean_intercept();
    intercept.setReadOnly(true);
    intercept.setLoaded();
    return sharableBean;
  }


}
