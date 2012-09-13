package com.avaje.ebean;

import junit.framework.TestCase;

import org.junit.Assert;

public class TestLogLevelOrdinalValue extends TestCase {
	
	public void testValues() {
		
		Assert.assertEquals(0,LogLevel.NONE.ordinal());
		Assert.assertEquals(1,LogLevel.SUMMARY.ordinal());
		Assert.assertEquals(2,LogLevel.SQL.ordinal());
		
	}

}
