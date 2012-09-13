package com.avaje.tests.lib;

import junit.framework.TestCase;

import org.junit.Assert;

import com.avaje.ebeaninternal.server.core.Message;

public class TestMessage extends TestCase {

	
	public void testMessage(){
		
		String one = "one";
		String two = "two";
		
		String m = Message.msg("fetch.error", one, two);
		boolean b = m.startsWith("Query threw SQLException:one Query was:");
		Assert.assertTrue(b);
	}
}
