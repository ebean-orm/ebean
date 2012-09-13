package com.avaje.tests.unitinternal;

import junit.framework.TestCase;

import org.junit.Assert;

import com.avaje.ebean.config.GlobalProperties;

public class TestGlobalPropsLoader extends TestCase {

	public void test() {
		
	    // this confused someone when looking at ebean.properties 
	    // so disabling this test
	    
	    boolean t = false;
	    if (t){
	        
	        // removing from ebean.properties
	        // ebean.properties.loader=com.avaje.tests.unitinternal.PropsLoader

    		GlobalProperties.put("ebean.ddl.run", "false");
    		
    		String s = GlobalProperties.get("robtest", null);
    		Assert.assertEquals("robvalue", s);
	    }
	}
	
	

}
