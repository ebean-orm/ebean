package org.tests.basic.one2one;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

public class TestOne2OneBookingInvoice extends BaseTestCase {

  @Test
  public void test() {

    Booking b = new Booking(3000L);
    Invoice ai = new Invoice();
    Invoice ci = new Invoice();

    ai.setBooking(b);
    ci.setBooking(b);

    b.setAgentInvoice(ai);
    b.setClientInvoice(ci);

    Ebean.save(b);

    Invoice invoice = Ebean.find(Invoice.class, ai.getId());
    assertEquals(b.getId(), invoice.getBooking().getId());

    Booking b1 = Ebean.find(Booking.class, b.getId());

    Invoice ai1 = b1.getAgentInvoice();
    assertNotNull(ai1);

    Booking b2 = ai1.getBooking();
    assertNotNull(b2);
    assertEquals(b1.getId(), b2.getId());
    assertSame(b1, b2);

    Invoice ci1 = b1.getClientInvoice();
    Booking b3 = ci1.getBooking();
    assertNotNull(b3);
    assertEquals(b1.getId(), b2.getId());
    assertSame(b1, b2);

    // cleanup
    ai.setBooking(null);
    ci.setBooking(null);
    Ebean.save(ai);
    Ebean.save(ci);
    Ebean.delete(b);
  }
}
