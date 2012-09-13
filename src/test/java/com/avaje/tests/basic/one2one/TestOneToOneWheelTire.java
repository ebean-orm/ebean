package com.avaje.tests.basic.one2one;

import junit.framework.TestCase;

import com.avaje.ebean.Ebean;

public class TestOneToOneWheelTire extends TestCase {

	public void test() {
		
	     Wheel w = new Wheel(); 
	     Tire t = new Tire(); 
	     t.setWheel(w); 
	     w.setTire(t); 
	     
	     Ebean.save(t);
	}
	
}
