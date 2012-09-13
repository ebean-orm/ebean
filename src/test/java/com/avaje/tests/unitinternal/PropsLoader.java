package com.avaje.tests.unitinternal;

import com.avaje.ebean.config.GlobalProperties;

/**
 * Simple example of using a Runnable to load the properties.
 */
public class PropsLoader implements Runnable {
		
	public void run() {
		GlobalProperties.put("robtest", "robvalue");
		GlobalProperties.put("robtest", "robvalue");
	}
		
}
