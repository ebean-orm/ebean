package com.avaje.tests.text.xml;

import junit.framework.TestCase;

import org.junit.Assert;

import com.avaje.ebeaninternal.server.lib.util.Dnode;
import com.avaje.ebeaninternal.server.lib.util.DnodeReader;

public class TestDnodeXmlParser extends TestCase {

	public void testSimple() {
		
		String xmlString = "<doc><div class=\"hello\"><h1>heading</h1><p>paragraph</p></div></doc>";
		
		DnodeReader r = new DnodeReader();
		Dnode n = r.parseXml(xmlString);
		
		String convertedTo = n.toXml();
		
		
		Assert.assertEquals(xmlString, convertedTo);
		
	}
	
}
