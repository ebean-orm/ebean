package com.avaje.ebeaninternal.server.core;

import java.util.ArrayList;
import java.util.List;

import com.avaje.ebean.config.GlobalProperties;

/**
 * Helper used in producing debug output on lazy loading.
 */
public class DebugLazyLoad {

	private final String[] ignoreList;
	
	private final boolean debug;

	public DebugLazyLoad(boolean lazyLoadDebug) {
		ignoreList = buildLazyLoadIgnoreList();
		debug = lazyLoadDebug;
	}

	/**
	 * Return true if debugging is on.
	 */
	public boolean isDebug() {
		return debug;
	}

	/**
	 * Return the StackTraceElement that is believed to be the line of code that
	 * triggered the lazy loading.
	 * <p>
	 * This is determined by going up the stack trace ignoring all the sections
	 * of java and Ebean code etc until you find code not in the ignore list -
	 * this is assumed to be your application code that triggered the lazy
	 * loading (it could be a third party layer such as a web template).
	 * </p>
	 */
	public StackTraceElement getStackTraceElement(Class<?> beanType) {

		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		for (int i = 0; i < stackTrace.length; i++) {
			if (isStackLine(stackTrace[i], beanType)) {
				return stackTrace[i];
			}
		}
		return null;
	}

	/**
	 * Return true if the code is expected to be application code.
	 */
	private boolean isStackLine(StackTraceElement element, Class<?> beanType) {
		
		String stackClass = element.getClassName();
		
		if (isBeanClass(beanType, stackClass)) {
			return false;
		}
		
		for (int i = 0; i < ignoreList.length; i++) {
			if (stackClass.startsWith(ignoreList[i])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Recurse up the inheritance checking to see if the stackClass is 
	 * this beanType (or a parent type).
	 */
	private boolean isBeanClass(Class<?> beanType, String stackClass) {
		if (stackClass.startsWith(beanType.getName())) {
			return true;
		}
		Class<?> superCls = beanType.getSuperclass();
		if (superCls.equals(Object.class)){
			return false;
		} else {
			return isBeanClass(superCls, stackClass);
		}
	}
	

	/**
	 * Build list of prefixes used to find the application code that triggered
	 * the lazy loading.
	 */
	private String[] buildLazyLoadIgnoreList() {

		List<String> ignore = new ArrayList<String>();

		// code that should be ignored when searching
		// the stack trace elements 
		ignore.add("com.avaje.ebean");
		ignore.add("java");
		ignore.add("sun.reflect");
		ignore.add("org.codehaus.groovy.runtime.");

		String extraIgnore = GlobalProperties.get("debug.lazyload.ignore", null);
		if (extraIgnore != null) {
			String[] split = extraIgnore.split(",");
			for (int i = 0; i < split.length; i++) {
				ignore.add(split[i].trim());
			}
		}

		return ignore.toArray(new String[ignore.size()]);
	}
}
