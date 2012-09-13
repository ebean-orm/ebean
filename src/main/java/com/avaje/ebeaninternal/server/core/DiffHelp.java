/**
 * Copyright (C) 2006  Robin Bygrave
 * 
 * This file is part of Ebean.
 * 
 * Ebean is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *  
 * Ebean is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Ebean; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA  
 */
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
	 * A and B must be of the same type. B can be null, in which case the
	 * 'OldValues' of a is used to compare with (as B).
	 * </p>
	 * <p>
	 * This intentionally does not include as OneToMany or ManyToMany
	 * properties.
	 * </p>
	 */
	public Map<String, ValuePair> diff(Object a, Object b, BeanDescriptor<?> desc) {

		boolean oldValues = false;
		if (b == null) {
			// get the old values from a
			if (a instanceof EntityBean) {
				EntityBean eb = (EntityBean) a;
				b = eb._ebean_getIntercept().getOldValues();
				oldValues = true;
			}
		}

		Map<String, ValuePair> map = new LinkedHashMap<String, ValuePair>();

		if (b == null) {
			return map;
		}

		// check the simple properties
		BeanProperty[] base = desc.propertiesBaseScalar();
		for (int i = 0; i < base.length; i++) {
			
			Object aval = base[i].getValue(a);
			Object bval = base[i].getValue(b);
			if (!ValueUtil.areEqual(aval, bval)) {
				map.put(base[i].getName(), new ValuePair(aval, bval));
			}
		}

		diffAssocOne(a, b, desc, map);
		diffEmbedded(a, b, desc, map, oldValues);

		return map;
	}

	/**
	 * Check the Embedded bean properties for differences.
	 * <p>
	 * If ANY of the properties are different then the whole Embedded bean is
	 * determined to be different as is added to the map.
	 * </p>
	 */
	private void diffEmbedded(Object a, Object b, BeanDescriptor<?> desc, Map<String, ValuePair> map,
			boolean oldValues) {

		BeanPropertyAssocOne<?>[] emb = desc.propertiesEmbedded();

		for (int i = 0; i < emb.length; i++) {
			Object aval = emb[i].getValue(a);
			Object bval = emb[i].getValue(b);
			if (oldValues) {
				bval = ((EntityBean) bval)._ebean_getIntercept().getOldValues();
				if (bval == null) {
					continue;
				}
			}

			if (!isBothNull(aval, bval)) {
				if (isDiffNull(aval, bval)) {
					// one of the embedded beans is null
					map.put(emb[i].getName(), new ValuePair(aval, bval));

				} else {
					// if ANY of the properties in an Embedded bean is
					// different, treat the whole bean as being different
					BeanProperty[] props = emb[i].getProperties();
					for (int j = 0; j < props.length; j++) {
						Object aEmbPropVal = props[j].getValue(aval);
						Object bEmbPropVal = props[j].getValue(bval);
						if (!ValueUtil.areEqual(aEmbPropVal, bEmbPropVal)) {

							// if one prop is different put the
							// embedded bean in the map
							map.put(emb[i].getName(), new ValuePair(aval, bval));
						}
					}
				}
			}
		}
	}

	/**
	 * If the properties are different by null OR if the id value is different,
	 * then add the Assoc One bean to the map.
	 */
	private void diffAssocOne(Object a, Object b, BeanDescriptor<?> desc, Map<String, ValuePair> map) {

		BeanPropertyAssocOne<?>[] ones = desc.propertiesOne();

		for (int i = 0; i < ones.length; i++) {
			Object aval = ones[i].getValue(a);
			Object bval = ones[i].getValue(b);

			if (!isBothNull(aval, bval)) {
				if (isDiffNull(aval, bval)) {
					// one of them is/was null
					map.put(ones[i].getName(), new ValuePair(aval, bval));

				} else {
					// check to see if the Id properties
					// are different
					BeanDescriptor<?> oneDesc = ones[i].getTargetDescriptor();
					Object aOneId = oneDesc.getId(aval);
					Object bOneId = oneDesc.getId(bval);

					if (!ValueUtil.areEqual(aOneId, bOneId)) {
						// the ids are different
						map.put(ones[i].getName(), new ValuePair(aval, bval));
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
