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
package com.avaje.ebeaninternal.server.el;

import java.util.Comparator;

/**
 * Comparator based on a ElGetValue.
 */
public final class ElComparatorProperty<T> implements Comparator<T>, ElComparator<T> {

	private final ElPropertyValue elGetValue;
	
	private final int nullOrder;
	
	private final int asc;
	
	public ElComparatorProperty(ElPropertyValue elGetValue, boolean ascending, boolean nullsHigh) {
		this.elGetValue = elGetValue;
		this.asc = ascending ? 1 : -1;
		this.nullOrder = asc * (nullsHigh ? 1 : -1);
	}

	public int compare(T o1, T o2) {
		
		Object val1 = elGetValue.elGetValue(o1);
		Object val2 = elGetValue.elGetValue(o2);

		return compareValues(val1, val2);
	}

	public int compareValue(Object value, T o2) {
		
		Object val2 = elGetValue.elGetValue(o2);

		return compareValues(value, val2);
	}	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public int compareValues(Object val1, Object val2){
		
		if (val1 == null){
			return val2 == null ? 0 : nullOrder;
		}
		if (val2 == null){
			return -1 * nullOrder;
		} 
		Comparable c = (Comparable)val1;
		return asc * c.compareTo(val2);		
	}

	
}
