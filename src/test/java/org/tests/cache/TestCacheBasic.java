package org.tests.cache;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheStatistics;
import org.tests.model.basic.Country;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

public class TestCacheBasic extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Ebean.getServerCacheManager().clear(Country.class);
    ServerCache countryCache = Ebean.getServerCacheManager().getBeanCache(Country.class);

    loadCountryCache();
    Assert.assertTrue(countryCache.size() > 0);

    // reset the statistics
    countryCache.getStatistics(true);

    Country c0 = Ebean.getReference(Country.class, "NZ");
    ServerCacheStatistics statistics = countryCache.getStatistics(false);
    long hc = statistics.getHitCount();
    Assert.assertEquals(1, hc);
    Assert.assertNotNull(c0);

    // Country c1 = Ebean.getReference(Country.class, "NZ");
    // Assert.assertEquals(2, countryCache.getStatistics(false).getHitCount());
    // //Assert.assertEquals(100,
    // countryCache.getStatistics(false).getHitRatio());
    //
    // // same instance as caching with readOnly=true
    // Assert.assertTrue(c0 != c1);
    //
    // c0.getName();
    // c1.getName();
    //
    // // reset the statistics
    // Assert.assertEquals(2,countryCache.getStatistics(true).getHitCount());
    // // now the count should be 0 again
    // Assert.assertEquals(0, countryCache.getStatistics(false).getHitCount());
    // // and hitRatio is 0 as well
    // Assert.assertEquals(0, countryCache.getStatistics(false).getHitRatio());
    //
    // // hit the country cache automatically via join
    //
    // Customer custTest = ResetBasicData.createCustAndOrder("cacheBasic");
    // Integer id = custTest.getId();
    // Customer customer = Ebean.find(Customer.class, id);
    //
    // Address billingAddress = customer.getBillingAddress();
    // Country c2 = billingAddress.getCountry();
    // c2.getName();
    //
    // Assert.assertTrue(countryCache.getStatistics(false).getHitCount() > 0);
    //
    // //Country c3 = Ebean.getReference(Country.class, "NZ");
    // //Country c4 = Ebean.find(Country.class, "NZ");
    //
    //
    // // clear the cache
    // Ebean.getServerCacheManager().clear(Country.class);
    // // reset statistics
    // countryCache.getStatistics(true);
    //
    // // try to hit the country cache automatically via join
    // customer = Ebean.find(Customer.class, id);
    // billingAddress = customer.getBillingAddress();
    // Country c5 = billingAddress.getCountry();
    // // but cache is empty so c5 is reference that will load cache
    // // if it is lazy loaded
    // Assert.assertEquals("empty cache",0,countryCache.getStatistics(false).getSize());
    // //Assert.assertEquals("missCount 1",1,countryCache.getStatistics(false).getMissCount());
    //
    // // lazy load on c5 populates the cache
    // c5.getName();
    // Assert.assertEquals("cache populated via lazy load",1,countryCache.getStatistics(false).getSize());
    //
    // // now these get hits in the cache
    // Country c6 = Ebean.find(Country.class, "NZ");
    //
    // Assert.assertTrue("different instance as cache cleared",c2 != c5);
    // Assert.assertTrue("these 2 are different",c5 != c6);
    //
    // // by default readOnly based on deployment annotation
    // Assert.assertTrue("read only",Ebean.getBeanState(c6).isReadOnly());
    //
    // try {
    // // can't modify a readOnly bean
    // c6.setName("Nu Zilund");
    // Assert.assertFalse("Never get here",true);
    // } catch (IllegalStateException e){
    // Assert.assertTrue("This is readOnly",true);
    // }
    //
    // Country c8 = Ebean.find(Country.class)
    // .setId("NZ")
    // .setReadOnly(false)
    // .findOne();
    //
    // // Explicitly NOT readOnly
    // Assert.assertFalse("NOT read only",Ebean.getBeanState(c8).isReadOnly());
    //
    // Assert.assertEquals("1 countries in cache", 1, countryCache.size());
    // c8.setName("Nu Zilund");
    // // the update will remove the entry from the cache
    // Ebean.save(c8);
    //
    // Assert.assertEquals("1 country in cache", 1, countryCache.size());
    //
    // Country c9 = Ebean.find(Country.class)
    // .setReadOnly(false)
    // .setId("NZ")
    // .findOne();
    //
    // // Find loads cache ...
    // Assert.assertFalse(Ebean.getBeanState(c9).isReadOnly());
    // Assert.assertTrue(countryCache.size() > 0);
    //
    // Country c10 = Ebean.find(Country.class,"NZ");
    //
    // Assert.assertTrue(Ebean.getBeanState(c10).isReadOnly());
    // Assert.assertTrue(countryCache.size() > 0);
    //
    // Ebean.getServerCacheManager().clear(Country.class);
    // Assert.assertEquals("0 country in cache", 0, countryCache.size());
    //
    // // reference doesn't load cache yet
    // Country c11 = Ebean.getReference(Country.class, "NZ");
    //
    // // still 0 in cache
    // Assert.assertEquals("0 country in cache", 0, countryCache.size());
    //
    // // will invoke lazy loading..
    // c11.getName();
    // Assert.assertTrue(countryCache.size() > 0);

  }
}
