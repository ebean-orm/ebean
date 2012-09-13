package com.avaje.tests.text.csv;

import java.sql.Time;

import junit.framework.TestCase;

import org.junit.Assert;

import com.avaje.ebean.text.TimeStringParser;

public class TestTimeStringParser extends TestCase {

	public void testSimple() {
		
		Time t = (Time)TimeStringParser.get().parse("12:00");
		Assert.assertNotNull(t);
		
		t = (Time)TimeStringParser.get().parse("12:00:12");
		Assert.assertNotNull(t);
		
		expectError("12");
		expectError("12:");
		expectError("12:00:");
		expectError("12:00::");
		expectError("12:00:00:");

	}
	
	private void expectError(String value) {
		try {
			TimeStringParser.get().parse(value);
			Assert.assertTrue(false);
		} catch (IllegalArgumentException e){
			Assert.assertTrue(true);
		}
	}
}
