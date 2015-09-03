package com.avaje.ebeaninternal.server.core;

import java.util.LinkedHashMap;
import java.util.Map;

import com.avaje.ebean.ValuePair;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import com.avaje.ebeaninternal.util.ValueUtil;

/**
 * Helper to perform a diff given two beans of the same type.
 * <p>
 * This intentionally does not include any OneToMany or ManyToMany properties.
 * </p>
 */
public class DiffHelp {

  private final boolean flatMode;

  public DiffHelp(boolean flatMode) {
    this.flatMode = flatMode;
  }

  /**
   * Return a map of the differences between a and b.
   * <p>
   * A and B must be of the same type. B can be null, in which case the 'dirty
   * values' of a is returned.
   * </p>
   * <p>
   * This intentionally does not include as OneToMany or ManyToMany properties.
   * </p>
   */
	public Map<String, ValuePair> diff(Object newBean, Object oldBean, BeanDescriptor<?> desc) {

    if (!(newBean instanceof EntityBean)) {
      throw new IllegalArgumentException("First bean expected to be an enhanced EntityBean? bean:"+newBean);
    }

    if (oldBean != null) {
      if (!(oldBean instanceof EntityBean)) {
        throw new IllegalArgumentException("Second bean expected to be an enhanced EntityBean? bean:"+oldBean);
      }
      if (!newBean.getClass().isAssignableFrom(oldBean.getClass())) {
        throw new IllegalArgumentException("Second bean not assignable to the first bean?");
      }
    }
    
		if (oldBean == null) {
			return ((EntityBean) newBean)._ebean_getIntercept().getDirtyValues();
		}

    Map<String, ValuePair> map = new LinkedHashMap<String, ValuePair>();
    diff(null, map, (EntityBean) newBean, (EntityBean) oldBean, desc);
    return map;
	}
	
	public void diff(String prefix, Map<String, ValuePair> map, EntityBean newBean, EntityBean oldBean, BeanDescriptor<?> desc) {

    if (flatMode) {
      desc.diff(prefix, map, newBean, oldBean);
    } else {

      // check the simple properties
      BeanProperty[] base = desc.propertiesBaseScalar();
      for (int i = 0; i < base.length; i++) {
        base[i].diff(prefix, map, newBean, oldBean);
      }

      diffAssocOne(prefix, newBean, oldBean, desc, map);
      diffEmbedded(prefix, newBean, oldBean, desc, map);
    }
	}

	/**
	 * Check the Embedded bean properties for differences.
	 * <p>
	 * If ANY of the properties are different then the whole Embedded bean is
	 * determined to be different as is added to the map.
	 * </p>
	 */
	private void diffEmbedded(String prefix, EntityBean newBean, EntityBean oldBean, BeanDescriptor<?> desc, Map<String, ValuePair> map) {

		BeanPropertyAssocOne<?>[] emb = desc.propertiesEmbedded();

		for (int i = 0; i < emb.length; i++) {
			EntityBean newVal = (EntityBean)emb[i].getValue(newBean);
			EntityBean oldVal = (EntityBean)emb[i].getValue(oldBean);
			
			if (!isBothNull(newVal, oldVal)) {
        String propName = (prefix == null) ? emb[i].getName() : prefix + emb[i].getName();
				if (isDiffNull(newVal, oldVal)) {
					// one of the embedded beans is null
          if (flatMode) {
            BeanDescriptor<?> embDesc = emb[i].getTargetDescriptor();
            diff(propName, map, newVal, oldVal, embDesc);
          } else {
            map.put(propName, new ValuePair(newVal, oldVal));
          }

				} else {
				  // recursively diff into the embedded bean
				  BeanDescriptor<?> embDesc = emb[i].getTargetDescriptor();
				  diff(propName, map, newVal, oldVal, embDesc);
				}
			}
		}
	}

	/**
	 * If the properties are different by null OR if the id value is different,
	 * then add the Assoc One bean to the map.
	 */
	private void diffAssocOne(String prefix, EntityBean newBean, EntityBean oldBean, BeanDescriptor<?> desc, Map<String, ValuePair> map) {

		BeanPropertyAssocOne<?>[] ones = desc.propertiesOne();

		for (int i = 0; i < ones.length; i++) {
			Object newVal = ones[i].getValue(newBean);
			Object oldVal = ones[i].getValue(oldBean);

			if (!isBothNull(newVal, oldVal)) {
        BeanDescriptor<?> oneDesc = ones[i].getTargetDescriptor();
        Object newId = (newVal == null) ? null : oneDesc.getId((EntityBean)newVal);
        Object oldId = (oldVal == null) ? null : oneDesc.getId((EntityBean)oldVal);

        if (!ValueUtil.areEqual(newId, oldId)) {
          String propName = (prefix == null) ? ones[i].getName() : prefix + ones[i].getName();
          // the ids are different
          if (flatMode) {
            String idName =  oneDesc.getIdProperty().getName();
            map.put(propName + "." + idName, new ValuePair(newId, oldId));

          } else {
            map.put(propName, new ValuePair(newVal, oldVal));
          }
        }
      }
		}
	}

	private boolean isBothNull(Object newVal, Object oldVal) {
		return newVal == null && oldVal == null;
	}

	private boolean isDiffNull(Object newVal, Object oldVal) {
		if (newVal == null) {
			return oldVal != null;
		} else {
			return oldVal == null;
		}
	}
}
