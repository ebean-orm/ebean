package com.avaje.ebeaninternal.server.deploy;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Helper object to find generic parameter types for a given class.
 */
public class ParamTypeUtil {

	/**
	 * Find and return the parameter type given a generic interface or class.
	 * <p>
	 * This assumes there is only one generic parameter.
	 * </p>
	 * <p>
	 * Returns null if no match was found.
	 * </p>
	 * @param cls the class to search for the parameter type
	 * @param matchType the type which has the generic parameter
	 */
	public static Class<?> findParamType(Class<?> cls, Class<?> matchType) {
		
		// search for: implementing a generic interface
		Type paramType = matchByInterfaces(cls, matchType);
		if (paramType == null){
			// search for: extending a generic class
			Type genericSuperclass = cls.getGenericSuperclass();
			if (genericSuperclass != null){
				paramType = matchParamType(genericSuperclass, matchType);				
			}
		}
		
		if (paramType instanceof Class<?>){
			// only interested in classes
			return (Class<?>)paramType;
		} else {
			return null;
		}
	}
	
	/**
	 * Check if the type is a generic one with parameters and of the correct type we are
	 * searching for. Return the parameter type if this matches otherwise return null.
	 */
	private static Type matchParamType(Type type, Class<?> matchType) {
		if (type instanceof ParameterizedType){
			ParameterizedType pt = (ParameterizedType)type;
			Type rawType = pt.getRawType();
			boolean isAssignable = matchType.isAssignableFrom((Class<?>) rawType);
			if (isAssignable) {
				// assume there is only one parameter type
				Type[] typeArguments = pt.getActualTypeArguments();
				if (typeArguments.length != 1){
					String m = "Expecting only 1 generic paramater but got "+typeArguments.length+" for "+type;
					throw new RuntimeException(m);
				}
				return typeArguments[0];
			}
		}
		return null;
	}
	
	/**
	 * Search the interfaces this class implements.
	 */
	private static Type matchByInterfaces(Class<?> cls, Class<?> matchType) {
		
		Type[] gis = cls.getGenericInterfaces();
		for (int i = 0; i < gis.length; i++) {
			Type match = matchParamType(gis[i], matchType);
			if (match != null){
				return match;
			}
		}
		return null;
	}
}
