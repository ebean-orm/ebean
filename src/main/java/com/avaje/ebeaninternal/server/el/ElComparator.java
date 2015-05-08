package com.avaje.ebeaninternal.server.el;

import java.util.Comparator;

/**
 * Comparator for use with the expression objects.
 */
public interface ElComparator<T> extends Comparator<T> {

	/**
	 * Compare given 2 beans.
	 */
	public int compare(T o1, T o2);

	/**
	 * Compare with a fixed value to a given bean.
	 */
	public int compareValue(Object value, T o2);

}