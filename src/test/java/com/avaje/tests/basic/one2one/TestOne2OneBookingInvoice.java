package com.avaje.tests.basic.one2one;

import com.avaje.ebean.Ebean;

import junit.framework.Assert;
import junit.framework.TestCase;

public class TestOne2OneBookingInvoice extends TestCase {
	
	public void test() {
		Booking b = new Booking();

		Invoice ai = new Invoice();
		Invoice ci = new Invoice();

		ai.setBooking(b);
		ci.setBooking(b);

		b.setAgentInvoice(ai);
		b.setClientInvoice(ci);

		Ebean.save(b);
		
		Booking b1 = Ebean.find(Booking.class, b.getId());
		
		Invoice ai1 = b1.getAgentInvoice();
		Assert.assertNotNull(ai1);
		
		Booking b2 = ai1.getBooking();
		Assert.assertNotNull(b2);
		Assert.assertEquals(b1.getId(), b2.getId());
		Assert.assertSame(b1, b2);
		
		Invoice ci1 = b1.getClientInvoice();
		Booking b3 = ci1.getBooking();
		Assert.assertNotNull(b3);
		Assert.assertEquals(b1.getId(), b2.getId());
		Assert.assertSame(b1, b2);
		
	}
}
