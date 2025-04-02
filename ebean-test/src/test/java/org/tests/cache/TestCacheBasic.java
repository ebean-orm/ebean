package org.tests.cache;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheStatistics;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Country;
import org.tests.model.basic.ResetBasicData;

import static org.junit.jupiter.api.Assertions.*;

public class TestCacheBasic extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    DB.cacheManager().clear(Country.class);
    ServerCache countryCache = DB.cacheManager().beanCache(Country.class);

    loadCountryCache();
    assertTrue(countryCache.size() > 0);

    // reset the statistics
    countryCache.statistics(true);

    Country c0 = DB.reference(Country.class, "NZ");
    ServerCacheStatistics statistics = countryCache.statistics(false);
    long hc = statistics.getHitCount();
    assertEquals(0, hc); // Change behaviour, reference() no longer hits cache with unmodifiable
    assertNotNull(c0);

    // Country c1 = DB.reference(Country.class, "NZ");
    // assertEquals(2, countryCache.getStatistics(false).getHitCount());
    // //assertEquals(100,
    // countryCache.getStatistics(false).getHitRatio());
    //
    // // same instance as caching with readOnly=true
    // assertTrue(c0 != c1);
    //
    // c0.getName();
    // c1.getName();
    //
    // // reset the statistics
    // assertEquals(2,countryCache.getStatistics(true).getHitCount());
    // // now the count should be 0 again
    // assertEquals(0, countryCache.getStatistics(false).getHitCount());
    // // and hitRatio is 0 as well
    // assertEquals(0, countryCache.getStatistics(false).getHitRatio());
    //
    // // hit the country cache automatically via join
    //
    // Customer custTest = ResetBasicData.createCustAndOrder("cacheBasic");
    // Integer id = custTest.getId();
    // Customer customer = DB.find(Customer.class, id);
    //
    // Address billingAddress = customer.getBillingAddress();
    // Country c2 = billingAddress.getCountry();
    // c2.getName();
    //
    // assertTrue(countryCache.getStatistics(false).getHitCount() > 0);
    //
    // //Country c3 = DB.reference(Country.class, "NZ");
    // //Country c4 = DB.find(Country.class, "NZ");
    //
    //
    // // clear the cache
    // DB.cacheManager().clear(Country.class);
    // // reset statistics
    // countryCache.getStatistics(true);
    //
    // // try to hit the country cache automatically via join
    // customer = DB.find(Customer.class, id);
    // billingAddress = customer.getBillingAddress();
    // Country c5 = billingAddress.getCountry();
    // // but cache is empty so c5 is reference that will load cache
    // // if it is lazy loaded
    // assertEquals("empty cache",0,countryCache.getStatistics(false).getSize());
    // //assertEquals("missCount 1",1,countryCache.getStatistics(false).getMissCount());
    //
    // // lazy load on c5 populates the cache
    // c5.getName();
    // assertEquals("cache populated via lazy load",1,countryCache.getStatistics(false).getSize());
    //
    // // now these get hits in the cache
    // Country c6 = DB.find(Country.class, "NZ");
    //
    // assertTrue("different instance as cache cleared",c2 != c5);
    // assertTrue("these 2 are different",c5 != c6);
    //
    // // by default readOnly based on deployment annotation
    // assertTrue("read only",DB.beanState(c6).isReadOnly());
    //
    // try {
    // // can't modify a readOnly bean
    // c6.setName("Nu Zilund");
    // assertFalse("Never get here",true);
    // } catch (IllegalStateException e){
    // assertTrue("This is readOnly",true);
    // }
    //
    // Country c8 = DB.find(Country.class)
    // .setId("NZ")
    // .setReadOnly(false)
    // .findOne();
    //
    // // Explicitly NOT readOnly
    // assertFalse("NOT read only",DB.beanState(c8).isReadOnly());
    //
    // assertEquals("1 countries in cache", 1, countryCache.size());
    // c8.setName("Nu Zilund");
    // // the update will remove the entry from the cache
    // DB.save(c8);
    //
    // assertEquals("1 country in cache", 1, countryCache.size());
    //
    // Country c9 = DB.find(Country.class)
    // .setReadOnly(false)
    // .setId("NZ")
    // .findOne();
    //
    // // Find loads cache ...
    // assertFalse(DB.beanState(c9).isReadOnly());
    // assertTrue(countryCache.size() > 0);
    //
    // Country c10 = DB.find(Country.class,"NZ");
    //
    // assertTrue(DB.beanState(c10).isReadOnly());
    // assertTrue(countryCache.size() > 0);
    //
    // DB.cacheManager().clear(Country.class);
    // assertEquals("0 country in cache", 0, countryCache.size());
    //
    // // reference doesn't load cache yet
    // Country c11 = DB.reference(Country.class, "NZ");
    //
    // // still 0 in cache
    // assertEquals("0 country in cache", 0, countryCache.size());
    //
    // // will invoke lazy loading..
    // c11.getName();
    // assertTrue(countryCache.size() > 0);

  }
}
