package com.avaje.ebeaninternal.server.persist;


/**
 * Utility object with helper methods for DML. 
 */
public class DmlUtil {

	/**
	 * Return true if the value is null or a Numeric 0 (for primitive int's and long's) or Option empty.
	 */
	public static boolean isNullOrZero(Object value){
		if (value == null){
			return true;
		}

		if (value instanceof Number){
			return ((Number)value).longValue() == 0l;
		}
		
		return false;
	}
}
