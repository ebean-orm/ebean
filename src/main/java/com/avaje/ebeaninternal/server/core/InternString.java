package com.avaje.ebeaninternal.server.core;

import java.util.HashMap;

/**
 * Used to reduce memory consumption of strings used in deployment processing.
 * <p>
 * Using this for now instead of String.intern() to avoid any unexpected 
 * increase in PermGen space.
 * </p>
 */
public final class InternString {

	private static HashMap<String,String> map = new HashMap<String,String>();
	
	
	/**
	 * Return the shared instance of this string.
	 */
	public static String intern(String s){
		
		if (s == null){
			return null;
		}
		
		synchronized (map) {
			String v = map.get(s);
			if (v != null){
				return v;
			} else {
				map.put(s, s);
				return s;
			}
			
		}
	}
}
