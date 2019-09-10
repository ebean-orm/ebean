package org.tests.query.embedded;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheManager;
import io.ebean.cache.ServerCacheStatistics;
import org.tests.model.embedded.EAddress;
import org.tests.model.embedded.EInvoice;
import org.tests.model.embedded.EInvoice.State;
import org.junit.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestMultipleEmbeddedLoading extends BaseTestCase {

  @Test
  public void testSimpleCase() {

    // prepare test
    EAddress ship = new EAddress();
    ship.setStreet("1 Banana St");
    ship.setSuburb("Suburb");
    ship.setCity("Auckland");

    EAddress bill = new EAddress();
    bill.setStreet("2 Apple St");
    bill.setSuburb("Suburb");
    bill.setCity("Auckland");

    EInvoice invoice = new EInvoice();
    invoice.setInvoiceDate(new Date(System.currentTimeMillis()));
    invoice.setState(State.New);
    invoice.setShipAddress(ship);
    invoice.setBillAddress(bill);

    // act: save and fetch
    Ebean.save(invoice);

    EInvoice invoice2 = Ebean.find(EInvoice.class, invoice.getId());

    // assert fetched bean populated as expected
    assertEquals(invoice.getId(), invoice2.getId());
    assertEquals(invoice.getState(), invoice2.getState());
    assertThat(invoice2.getInvoiceDate()).isEqualToIgnoringMillis(invoice.getInvoiceDate());
    assertEquals("2 Apple St", invoice.getBillAddress().getStreet());
    assertEquals("2 Apple St", invoice2.getBillAddress().getStreet());

    // act: only update one of the embedded fields
    invoice2.getBillAddress().setStreet("3 Pineapple St");
    // bean should be dirty
    Ebean.save(invoice2);

    awaitL2Cache();

    EInvoice invoice3 = Ebean.find(EInvoice.class, invoice.getId());

    // assert field has updated value
    assertEquals("3 Pineapple St", invoice3.getBillAddress().getStreet());


    // fetch a partial
    EInvoice invoicePartial = Ebean.find(EInvoice.class)
      .select("state, date")
      .where().idEq(invoice.getId())
      .findOne();

    // lazy load of embedded bean
    EAddress billAddress = invoicePartial.getBillAddress();

    assertNotNull(billAddress);
    assertEquals("3 Pineapple St", billAddress.getStreet());

    EbeanServer server = Ebean.getServer(null);
    ServerCacheManager serverCacheManager = server.getServerCacheManager();

    // get cache, clear the cache and statistics
    ServerCache beanCache = serverCacheManager.getBeanCache(EInvoice.class);
    beanCache.clear();
    beanCache.getStatistics(true);

    // fetch and load the cache
    EInvoice invoice4 = Ebean.find(EInvoice.class, invoice.getId());
    assertNotNull(invoice4);

    ServerCacheStatistics statistics = beanCache.getStatistics(false);

    assertEquals(1, statistics.getSize());
    assertEquals(0, statistics.getHitCount());

    // fetch out of the cache this time
    EInvoice invoice5 = Ebean.find(EInvoice.class, invoice.getId());
    assertNotNull(invoice5);

    statistics = beanCache.getStatistics(false);
    assertEquals(1, statistics.getSize());
    assertEquals(1, statistics.getHitCount());

    billAddress = invoice5.getBillAddress();

    assertNotNull(billAddress);
    assertEquals("3 Pineapple St", billAddress.getStreet());

  }

}
