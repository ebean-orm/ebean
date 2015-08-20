package com.avaje.ebeaninternal.server.core;

import com.avaje.ebean.ValuePair;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocOne;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Helper to perform a 'diff' for an insert.
 * <p>
 * This intentionally does not include any OneToMany or ManyToMany properties.
 * </p>
 */
public class DiffHelpInsert {

  private DiffHelpInsert() {
  }

  /**
   * Return a map of the differences between a and b.
   * <p>
   * A and B must be of the same type. B can be null, in which case the 'dirty
   * diff' of a is returned.
   * </p>
   * <p>
   * This intentionally does not include as OneToMany or ManyToMany properties.
   * </p>
   */
	public static Map<String, ValuePair> diff(EntityBean newBean, BeanDescriptor<?> desc) {

    Map<String, ValuePair> map = new LinkedHashMap<String, ValuePair>();
    diff(null, map, newBean, desc);
    return map;
	}
	
	private static void diff(String prefix, Map<String, ValuePair> map, EntityBean newBean,  BeanDescriptor<?> desc) {

		// check the simple properties
		BeanProperty[] base = desc.propertiesBaseScalar();
		for (int i = 0; i < base.length; i++) {
			Object newVal = (newBean == null) ? null : base[i].getValue(newBean);
			if (newVal != null) {
			  String propName = (prefix == null) ? base[i].getName() : prefix + base[i].getName();
				map.put(propName, new ValuePair(newVal, null));
			}
		}

		diffAssocOne(prefix, newBean, desc, map);
		diffEmbedded(prefix, newBean, desc, map);
	}

	/**
	 * Check the Embedded bean properties for differences.
	 * <p>
	 * If ANY of the properties are different then the whole Embedded bean is
	 * determined to be different as is added to the map.
	 * </p>
	 */
	private static void diffEmbedded(String prefix, EntityBean newBean, BeanDescriptor<?> desc, Map<String, ValuePair> map) {

    BeanPropertyAssocOne<?>[] emb = desc.propertiesEmbedded();

    for (int i = 0; i < emb.length; i++) {
      EntityBean newVal = (EntityBean) emb[i].getValue(newBean);

      if (newVal != null) {
        String propName = (prefix == null) ? emb[i].getName() : prefix + emb[i].getName();

        BeanDescriptor<?> embDesc = emb[i].getTargetDescriptor();
        diff(propName + ".", map, newVal, embDesc);
      }
    }
  }

	/**
	 * If the properties are different by null OR if the id value is different,
	 * then add the Assoc One bean to the map.
	 */
	private static void diffAssocOne(String prefix, EntityBean newBean, BeanDescriptor<?> desc, Map<String, ValuePair> map) {

		BeanPropertyAssocOne<?>[] ones = desc.propertiesOne();

		for (int i = 0; i < ones.length; i++) {
			Object newVal = ones[i].getValue(newBean);

			if (newVal != null) {
        BeanDescriptor<?> oneDesc = ones[i].getTargetDescriptor();
        Object newId = oneDesc.getId((EntityBean)newVal);
        if (newId != null) {
          String propName = (prefix == null) ? ones[i].getName() : prefix + ones[i].getName();
          String idName =  oneDesc.getIdProperty().getName();
          map.put(propName + "." + idName, new ValuePair(newId, null));
        }
      }
		}
	}

}
