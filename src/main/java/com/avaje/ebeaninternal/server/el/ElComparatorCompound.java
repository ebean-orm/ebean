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
