package com.avaje.ebeaninternal.server.cache;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;

/**
 * Create a new CachedBeanData based on the existing CachedBeanData and the updated bean.
 */
public class CachedBeanDataUpdate {

  /**
   * Create a new CachedBeanData based on the existing CachedBeanData and the updated bean.
   */
  public static CachedBeanData update(BeanDescriptor<?> desc, CachedBeanData existingData, EntityBean updateBean) {

    // take a copy of the raw data and loaded status
    boolean[] copyLoaded = existingData.copyLoaded();
    Object[] copyData = existingData.copyData();

    EntityBeanIntercept ebi = updateBean._ebean_getIntercept();

    Object newNaturalKey = null;
    Object oldNaturalKey = existingData.getNaturalKey();

    BeanProperty[] props = desc.propertiesNonMany();
    for (int i = 0; i < props.length; i++) {
      // check if the properties was in the update
      int propertyIndex = props[i].getPropertyIndex();
      if (ebi.isLoadedProperty(propertyIndex)) {
        if (props[i].isNaturalKey()) {
          newNaturalKey = updateBean._ebean_getField(propertyIndex);
        }
        // set the cache safe value for the property and mark it as loaded
        copyData[propertyIndex] = props[i].getCacheDataValue(updateBean);
        copyLoaded[propertyIndex] = true;
      }
    }

    return new CachedBeanData(null, copyLoaded, copyData, newNaturalKey, oldNaturalKey);
  }

}