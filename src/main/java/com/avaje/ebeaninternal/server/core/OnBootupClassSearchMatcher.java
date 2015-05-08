package com.avaje.ebeaninternal.server.core;

import com.avaje.ebeaninternal.server.util.ClassPathSearchMatcher;

/**
 * Matcher used for searching for Embeddable, Entity and ScalarTypes in the
 * class path.
 */
public class OnBootupClassSearchMatcher implements ClassPathSearchMatcher {

	BootupClasses classes = new BootupClasses();
	
	public boolean isMatch(Class<?> cls) {

		return classes.isMatch(cls);
	}
	
	public BootupClasses getOnBootupClasses() {
		return classes;
	}

}
