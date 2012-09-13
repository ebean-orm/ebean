package com.avaje.tests.basic.type;

import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.config.GlobalProperties;

public class TestTransientMap extends TestCase {

	public void testMe() {
		
		GlobalProperties.put("classes", BSimpleWithGen.class.toString());
        
		BSimpleWithGen b = new BSimpleWithGen();
		b.setName("blah");
		
		Ebean.save(b);
		
	}
}
