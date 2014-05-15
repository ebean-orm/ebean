package com.avaje.tests.query.embedded;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.cache.ServerCache;
import com.avaje.ebean.cache.ServerCacheManager;
import com.avaje.ebean.cache.ServerCacheStatistics;
import com.avaje.tests.model.embedded.EAddress;
import com.avaje.tests.model.embedded.EInvoice;
import com.avaje.tests.model.embedded.EInvoice.State;

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
    invoice.setDate(new Date(System.currentTimeMillis()));
    invoice.setState(State.New);
    invoice.setShipAddress(ship);
    invoice.setBillAddress(bill);
    
    // act: save and fetch
    Ebean.save(invoice);
    
    EInvoice invoice2 = Ebean.find(EInvoice.class)
      .where().idEq(invoice.getId())
      .findUnique();
    
    // assert fetched bean populated as expected
    Assert.assertEquals(invoice.getId(), invoice2.getId());
    Assert.assertEquals(invoice.getState(), invoice2.getState());
    Assert.assertEquals(invoice.getDate(), invoice2.getDate());
    Assert.assertEquals("2 Apple St", invoice.getBillAddress().getStreet());
    Assert.assertEquals("2 Apple St", invoice2.getBillAddress().getStreet());
    
    // act: only update one of the embedded fields
    invoice2.getBillAddress().setStreet("3 Pineapple St");
    // bean should be dirty
    Ebean.save(invoice2);
    
    EInvoice invoice3 = Ebean.find(EInvoice.class)
        .where().idEq(invoice.getId())
        .findUnique();
    
    // assert field has updated value
    Assert.assertEquals("3 Pineapple St", invoice3.getBillAddress().getStreet());
    
    
    // fetch a partial
    EInvoice invoicePartial = Ebean.find(EInvoice.class)
        .select("state, date")
        .where().idEq(invoice.getId())
        .findUnique();
    
    // lazy load of embedded bean
    EAddress billAddress = invoicePartial.getBillAddress();
      
    Assert.assertNotNull(billAddress);
    Assert.assertEquals("3 Pineapple St", billAddress.getStreet());
   
    EbeanServer server = Ebean.getServer(null);
    ServerCacheManager serverCacheManager = server.getServerCacheManager();
    
    // get cache, clear the cache and statistics
    ServerCache beanCache = serverCacheManager.getBeanCache(EInvoice.class);
    beanCache.clear();
    beanCache.getStatistics(true);
    
    // fetch and load the cache
    EInvoice invoice4 = Ebean.find(EInvoice.class)
        .where().idEq(invoice.getId())
        .setUseCache(true)
        .findUnique();
    
    Assert.assertNotNull(invoice4);
    
    ServerCacheStatistics statistics = beanCache.getStatistics(false);

    Assert.assertEquals(1, statistics.getSize());
    Assert.assertEquals(0, statistics.getHitCount());

    // fetch out of the cache this time
    EInvoice invoice5 = Ebean.find(EInvoice.class)
        .where().idEq(invoice.getId())
        .setUseCache(true)
        .findUnique();

    Assert.assertNotNull(invoice5);
    
    statistics = beanCache.getStatistics(false);
    Assert.assertEquals(1, statistics.getSize());
    Assert.assertEquals(1, statistics.getHitCount());

    billAddress = invoice5.getBillAddress();
    
    Assert.assertNotNull(billAddress);
    Assert.assertEquals("3 Pineapple St", billAddress.getStreet());

  }
  
}
