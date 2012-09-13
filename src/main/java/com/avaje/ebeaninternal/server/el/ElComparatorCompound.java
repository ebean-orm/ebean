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
 * Comparator based on multiple ordered comparators.
 * <p>
 * eg.  "name, orderDate desc, id"
 * </p>
 */
public final class ElComparatorCompound<T> implements Comparator<T>, ElComparator<T> {

	private final ElComparator<T>[] array;
	
	public ElComparatorCompound(ElComparator<T>[] array) {
		this.array = array;
	}
	
	public int compare(T o1, T o2) {
		
		for (int i = 0; i < array.length; i++) {
			int ret = array[i].compare(o1, o2);
			if (ret != 0){
				return ret;
			}
		}
		
		return 0;
	}

	public int compareValue(Object value, T o2) {
		
		for (int i = 0; i < array.length; i++) {
			int ret = array[i].compareValue(value, o2);
			if (ret != 0){
				return ret;
			}
		}
		
		return 0;
	}



	
	
	
}
