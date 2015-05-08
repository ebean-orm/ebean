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
	public Map<String, ValuePair> diff(Object a, Object b, BeanDescriptor<?> desc) {

    if (a instanceof EntityBean == false) {
      throw new IllegalArgumentException("First bean expected to be an enhanced EntityBean? bean:"+a);
    }

    if (b != null) {
      if (b instanceof EntityBean == false) {
        throw new IllegalArgumentException("Second bean expected to be an enhanced EntityBean? bean:"+b);
      }
      if (!a.getClass().isAssignableFrom(b.getClass())) {
        throw new IllegalArgumentException("Second bean not assignable to the first bean?");
      }
    }
    
		if (b == null) {
			return ((EntityBean) a)._ebean_getIntercept().getDirtyValues();
		}

    Map<String, ValuePair> map = new LinkedHashMap<String, ValuePair>();
    diff(null, map, (EntityBean)a, (EntityBean)b, desc);
    return map;
	}
	
	public void diff(String prefix, Map<String, ValuePair> map, EntityBean first, EntityBean sec, BeanDescriptor<?> desc) {

		// check the simple properties
		BeanProperty[] base = desc.propertiesBaseScalar();
		for (int i = 0; i < base.length; i++) {
			Object aval = base[i].getValue(first);
			Object bval = base[i].getValue(sec);
			if (!ValueUtil.areEqual(aval, bval)) {
			  String propName = (prefix == null) ? base[i].getName() : prefix + base[i].getName();
				map.put(propName, new ValuePair(aval, bval));
			}
		}

		diffAssocOne(prefix, first, sec, desc, map);
		diffEmbedded(prefix, first, sec, desc, map);
	}

	/**
	 * Check the Embedded bean properties for differences.
	 * <p>
	 * If ANY of the properties are different then the whole Embedded bean is
	 * determined to be different as is added to the map.
	 * </p>
	 */
	private void diffEmbedded(String prefix, EntityBean a, EntityBean b, BeanDescriptor<?> desc, Map<String, ValuePair> map) {

		BeanPropertyAssocOne<?>[] emb = desc.propertiesEmbedded();

		for (int i = 0; i < emb.length; i++) {
			EntityBean aval = (EntityBean)emb[i].getValue(a);
			EntityBean bval = (EntityBean)emb[i].getValue(b);
			
			if (!isBothNull(aval, bval)) {
        String propName = (prefix == null) ? emb[i].getName() : prefix + emb[i].getName();
				if (isDiffNull(aval, bval)) {
					// one of the embedded beans is null
					map.put(propName, new ValuePair(aval, bval));

				} else {
				  // recursively diff into the embedded bean
				  BeanDescriptor<?> embDesc = emb[i].getTargetDescriptor();
				  diff(emb[i].getName()+".", map, aval, bval, embDesc);
				}
			}
		}
	}

	/**
	 * If the properties are different by null OR if the id value is different,
	 * then add the Assoc One bean to the map.
	 */
	private void diffAssocOne(String prefix, EntityBean a, EntityBean b, BeanDescriptor<?> desc, Map<String, ValuePair> map) {

		BeanPropertyAssocOne<?>[] ones = desc.propertiesOne();

		for (int i = 0; i < ones.length; i++) {
			Object aval = ones[i].getValue(a);
			Object bval = ones[i].getValue(b);

			if (!isBothNull(aval, bval)) {
        String propName = (prefix == null) ? ones[i].getName() : prefix + ones[i].getName();
				if (isDiffNull(aval, bval)) {
					// one of them is/was null
					map.put(propName, new ValuePair(aval, bval));

				} else {
					// check to see if the Id properties
					// are different
					BeanDescriptor<?> oneDesc = ones[i].getTargetDescriptor();
					Object aOneId = oneDesc.getId((EntityBean)aval);
					Object bOneId = oneDesc.getId((EntityBean)bval);

					if (!ValueUtil.areEqual(aOneId, bOneId)) {
						// the ids are different
						map.put(propName, new ValuePair(aval, bval));
					}
				}
			}
		}
	}

	private boolean isBothNull(Object aval, Object bval) {
		return aval == null && bval == null;
	}

	private boolean isDiffNull(Object aval, Object bval) {
		if (aval == null) {
			return bval != null;
		} else {
			return bval == null;
		}
	}
}
