
package com.avaje.tests.unitinternal;

import junit.framework.Assert;
import junit.framework.TestCase;


public class TestBasics extends TestCase {


	private String parentPath(String path) {
		
		int pos = path.lastIndexOf('.');
		if (pos == -1){
			return null;			
		} else {
			return path.substring(0, pos);
		}		
	}
	
	public void testParentPath() {
		
		Assert.assertTrue((parentPath("banana") == null));
		Assert.assertTrue((parentPath("banana.apple").equals("banana")));
		Assert.assertTrue((parentPath("banana.apple.o").equals("banana.apple")));
		
	}
}
