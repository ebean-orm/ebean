package org.tests.query.embedded;

import io.ebean.CacheMode;
import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheManager;
import io.ebean.cache.ServerCacheStatistics;
import org.junit.jupiter.api.Test;
import org.tests.model.embedded.EAddress;
import org.tests.model.embedded.EInvoice;
import org.tests.model.embedded.EInvoice.State;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TestMultipleEmbeddedLoading extends BaseTestCase {

  @Test
  void testSimpleCase() {

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
    DB.save(invoice);

    EInvoice invoice2 = DB.find(EInvoice.class, invoice.getId());

    // assert fetched bean populated as expected
    assertEquals(invoice.getId(), invoice2.getId());
    assertEquals(invoice.getState(), invoice2.getState());
    assertThat(invoice2.getInvoiceDate()).isEqualToIgnoringMillis(invoice.getInvoiceDate());
    assertEquals("2 Apple St", invoice.getBillAddress().getStreet());
    assertEquals("2 Apple St", invoice2.getBillAddress().getStreet());

    EInvoice readOnlyInvoice = DB.find(EInvoice.class)
      .setId(invoice.getId())
      .setUnmodifiable(true)
      .setBeanCacheMode(CacheMode.OFF)
      .findOne();

    assertThatThrownBy(() -> readOnlyInvoice.getBillAddress().setCity("junk"))
      .describedAs("embedded bean is unmodifiable")
      .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> readOnlyInvoice.getShipAddress().setCity("junk"))
      .describedAs("embedded bean is unmodifiable")
      .isInstanceOf(UnsupportedOperationException.class);

    // act: only update one of the embedded fields
    invoice2.getBillAddress().setStreet("3 Pineapple St");
    // bean should be dirty
    DB.save(invoice2);

    awaitL2Cache();

    EInvoice invoice3 = DB.find(EInvoice.class, invoice.getId());

    // assert field has updated value
    assertEquals("3 Pineapple St", invoice3.getBillAddress().getStreet());


    // fetch a partial
    EInvoice invoicePartial = DB.find(EInvoice.class)
      .select("state, date")
      .where().idEq(invoice.getId())
      .findOne();

    // lazy load of embedded bean
    EAddress billAddress = invoicePartial.getBillAddress();

    assertNotNull(billAddress);
    assertEquals("3 Pineapple St", billAddress.getStreet());

    Database server = DB.getDefault();
    ServerCacheManager serverCacheManager = server.cacheManager();

    // get cache, clear the cache and statistics
    ServerCache beanCache = serverCacheManager.beanCache(EInvoice.class);
    beanCache.clear();
    beanCache.statistics(true);

    // fetch and load the cache
    EInvoice invoice4 = DB.find(EInvoice.class, invoice.getId());
    assertNotNull(invoice4);

    ServerCacheStatistics statistics = beanCache.statistics(false);

    assertEquals(1, statistics.getSize());
    assertEquals(0, statistics.getHitCount());

    // fetch out of the cache this time
    EInvoice invoice5 = DB.find(EInvoice.class, invoice.getId());
    assertNotNull(invoice5);

    statistics = beanCache.statistics(false);
    assertEquals(1, statistics.getSize());
    assertEquals(1, statistics.getHitCount());

    billAddress = invoice5.getBillAddress();

    assertNotNull(billAddress);
    assertEquals("3 Pineapple St", billAddress.getStreet());

  }

}
