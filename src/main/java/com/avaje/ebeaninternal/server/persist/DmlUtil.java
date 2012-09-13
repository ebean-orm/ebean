package com.avaje.ebeaninternal.server.persist;

import com.avaje.ebeaninternal.server.deploy.parse.DetectScala;

/**
 * Utility object with helper methods for DML. 
 */
public class DmlUtil {

    private static final boolean hasScalaSupport = DetectScala.hasScalaSupport();
    
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

		if (hasScalaSupport){
            if (value instanceof scala.Option<?>) {
                if (((scala.Option<?>) value).isEmpty()) {
                    return true;
                }
            }
		}
		
		return false;
	}
}
