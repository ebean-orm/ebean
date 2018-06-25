package org.tests.cache;

import io.ebean.BaseTestCase;
import io.ebean.CacheMode;
import io.ebean.Ebean;
import io.ebean.SqlUpdate;
import io.ebean.Transaction;
import io.ebean.Update;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheManager;
import io.ebeaninternal.server.cache.CachedManyIds;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Country;
import org.tests.model.basic.Customer;
import org.tests.model.basic.OCachedBean;
import org.tests.model.basic.Order;
import org.tests.model.basic.OrderDetail;
import org.tests.model.basic.ResetBasicData;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestCacheCollectionIds extends BaseTestCase {

  private ServerCacheManager cacheManager = Ebean.getServerCacheManager();

  @Test
  public void test() {

    ResetBasicData.reset();
    awaitL2Cache();

    ServerCache custCache = cacheManager.getBeanCache(Customer.class);
    ServerCache custManyIdsCache = cacheManager.getCollectionIdsCache(Customer.class, "contacts");

    // cacheManager.setCaching(Customer.class, true);
    // cacheManager.setCaching(Contact.class, true);

    custCache.clear();
    custManyIdsCache.clear();

    List<Customer> list = Ebean.find(Customer.class).setAutoTune(false).setBeanCacheMode(CacheMode.PUT)
      .order().asc("id").findList();

    assertTrue(list.size() > 1);
    // Assert.assertEquals(list.size(),
    // custCache.getStatistics(false).getSize());

    Customer customer = list.get(0);
    List<Contact> contacts = customer.getContacts();
    // Assert.assertEquals(0, custManyIdsCache.getStatistics(false).getSize());
    contacts.size();
    assertTrue(contacts.size() > 1);
    // Assert.assertEquals(1, custManyIdsCache.getStatistics(false).getSize());
    // Assert.assertEquals(0,
    // custManyIdsCache.getStatistics(false).getHitCount());

    fetchCustomer(customer.getId());
    // Assert.assertEquals(1,
    // custManyIdsCache.getStatistics(false).getHitCount());

    fetchCustomer(customer.getId());
    // Assert.assertEquals(2,
    // custManyIdsCache.getStatistics(false).getHitCount());

    int currentNumContacts = fetchCustomer(customer.getId());
    // Assert.assertEquals(3,
    // custManyIdsCache.getStatistics(false).getHitCount());

    Contact newContact = ResetBasicData.createContact("Check", "CollIds");
    newContact.setCustomer(customer);

    Ebean.save(newContact);
    awaitL2Cache();

    int currentNumContacts2 = fetchCustomer(customer.getId());
    assertEquals(currentNumContacts + 1, currentNumContacts2);

    // cleanup
    Ebean.delete(newContact);
  }

  private int fetchCustomer(Integer id) {

    Customer customer2 = Ebean.find(Customer.class, id);

    List<Contact> contacts2 = customer2.getContacts();

    for (Contact contact : contacts2) {
      contact.getFirstName();
      contact.getEmail();
    }
    return contacts2.size();
  }

  /**
   * When updating a ManyToMany relations also the collection cache must be updated.
   */
  @Test
  public void testUpdatingCollectionCacheForManyToManyRelations() {
    // arrange
    ResetBasicData.reset();

    OCachedBean cachedBean = new OCachedBean();
    cachedBean.setName("hello");
    cachedBean.getCountries().add(Ebean.find(Country.class, "NZ"));
    cachedBean.getCountries().add(Ebean.find(Country.class, "AU"));

    Ebean.save(cachedBean);

    // used to just load the cache - trigger loading
    OCachedBean dummyToLoad = Ebean.find(OCachedBean.class, cachedBean.getId());
    dummyToLoad.getCountries().size();

    ServerCache cachedBeanCountriesCache = cacheManager.getCollectionIdsCache(OCachedBean.class, "countries");
    CachedManyIds cachedManyIds = (CachedManyIds) cachedBeanCountriesCache.get(cachedBean.getId());

    // confirm the starting data and cache entry
    assertEquals(2, dummyToLoad.getCountries().size());
    assertEquals(2, cachedManyIds.getIdList().size());


    // act
    OCachedBean loadedBean = Ebean.find(OCachedBean.class, cachedBean.getId());
    loadedBean.getCountries().clear();
    loadedBean.getCountries().add(Ebean.find(Country.class, "AU"));

    Ebean.save(loadedBean);
    awaitL2Cache();

    // Get the data to assert/check against
    OCachedBean result = Ebean.find(OCachedBean.class, cachedBean.getId());
    cachedManyIds = (CachedManyIds) cachedBeanCountriesCache.get(result.getId());

    // assert that data and cache both show correct data
    assertEquals(1, result.getCountries().size());
    assertEquals(1, cachedManyIds.getIdList().size());
    assertFalse(cachedManyIds.getIdList().contains("NZ"));
    assertTrue(cachedManyIds.getIdList().contains("AU"));
  }


  /**
   * When updating a ManyToMany relations also the collection cache must be updated.
   * Alternate to above test where in this case the bean is dirty - loadedBean.setName("goodbye");.
   */
  @Test
  public void testUpdatingCollectionCacheForManyToManyRelationsWithUpdatedBean() {
    // arrange
    ResetBasicData.reset();

    OCachedBean cachedBean = new OCachedBean();
    cachedBean.setName("hello");
    cachedBean.getCountries().add(Ebean.find(Country.class, "NZ"));
    cachedBean.getCountries().add(Ebean.find(Country.class, "AU"));

    Ebean.save(cachedBean);

    // used to just load the cache - trigger loading
    OCachedBean dummyToLoad = Ebean.find(OCachedBean.class, cachedBean.getId());
    dummyToLoad.getCountries().size();

    ServerCache cachedBeanCountriesCache = cacheManager.getCollectionIdsCache(OCachedBean.class, "countries");
    CachedManyIds cachedManyIds = (CachedManyIds) cachedBeanCountriesCache.get(cachedBean.getId());

    // confirm the starting data and cache entry
    assertEquals(2, dummyToLoad.getCountries().size());
    assertEquals(2, cachedManyIds.getIdList().size());


    // act - this time update the name property so the bean is dirty
    OCachedBean loadedBean = Ebean.find(OCachedBean.class, cachedBean.getId());
    loadedBean.setName("goodbye");
    loadedBean.getCountries().clear();
    loadedBean.getCountries().add(Ebean.find(Country.class, "AU"));

    Ebean.save(loadedBean);
    awaitL2Cache();

    // Get the data to assert/check against
    OCachedBean result = Ebean.find(OCachedBean.class, cachedBean.getId());
    cachedManyIds = (CachedManyIds) cachedBeanCountriesCache.get(result.getId());

    // assert that data and cache both show correct data
    assertEquals(1, result.getCountries().size());
    assertEquals(1, cachedManyIds.getIdList().size());
    assertFalse(cachedManyIds.getIdList().contains("NZ"));
    assertTrue(cachedManyIds.getIdList().contains("AU"));
  }

  /**
   * When updating a ManyToMany relations also the collection cache must be updated.
   */
  @Test
  public void testUpdatingCollectionCacheForManyToManyRelationsWithinStatelessUpdate() {
    // arrange
    ResetBasicData.reset();

    OCachedBean cachedBean = new OCachedBean();
    cachedBean.setName("cachedBeanTest");
    cachedBean.getCountries().add(Ebean.find(Country.class, "NZ"));
    cachedBean.getCountries().add(Ebean.find(Country.class, "AU"));

    Ebean.save(cachedBean);

    // clear the cache
    ServerCache cachedBeanCountriesCache = cacheManager.getCollectionIdsCache(OCachedBean.class, "countries");
    cachedBeanCountriesCache.clear();
    assertEquals(0, cachedBeanCountriesCache.size());

    // load the cache
    OCachedBean dummyLoad = Ebean.find(OCachedBean.class, cachedBean.getId());
    List<Country> dummyCountries = dummyLoad.getCountries();
    assertEquals(2, dummyCountries.size());

    // assert that the cache contains the expected entry
    assertEquals("countries cache now loaded with 1 entry", 1, cachedBeanCountriesCache.size());
    CachedManyIds dummyEntry = (CachedManyIds) cachedBeanCountriesCache.get(dummyLoad.getId());
    Assert.assertNotNull(dummyEntry);
    assertEquals("2 ids in the entry", 2, dummyEntry.getIdList().size());
    assertTrue(dummyEntry.getIdList().contains("NZ"));
    assertTrue(dummyEntry.getIdList().contains("AU"));


    // act - this should invalidate our cache entry
    OCachedBean update = new OCachedBean();
    update.setId(cachedBean.getId());
    update.setName("modified");
    update.getCountries().add(Ebean.find(Country.class, "AU"));

    Ebean.update(update);
    awaitL2Cache();

    assertEquals("countries entry still there (but updated)", 1, cachedBeanCountriesCache.size());

    CachedManyIds cachedManyIds = (CachedManyIds) cachedBeanCountriesCache.get(update.getId());

    // assert cache updated
    assertEquals(1, cachedManyIds.getIdList().size());
    assertFalse(cachedManyIds.getIdList().contains("NZ"));
    assertTrue(cachedManyIds.getIdList().contains("AU"));

    // assert countries good
    OCachedBean result = Ebean.find(OCachedBean.class, cachedBean.getId());
    assertEquals(1, result.getCountries().size());

  }

  @Test
  public void testUpdating_noChange() throws InterruptedException {

    ResetBasicData.reset();

    try (Transaction txn = Ebean.beginTransaction()) {

      OCachedBean cachedBean = new OCachedBean();
      cachedBean.setName("helloForUpdate");
      Ebean.save(cachedBean);

      cachedBean = Ebean.find(OCachedBean.class, cachedBean.getId());

      cachedBean.setName("mod");
      ArrayList<Country> list = new ArrayList<>();
      list.add(Ebean.find(Country.class, "NZ"));
      cachedBean.setCountries(list);
      Ebean.save(cachedBean);


      cachedBean.setName("mod2");
      Ebean.save(cachedBean);

      txn.commit();
    }


    Thread.sleep(2000);
  }

  /**
   * When doing an ORM update the collection cache must be cleared.
   */
  @Test
  @Ignore
  public void testClearingCollectionCacheOnORMUpdate() {
    // arrange
    ResetBasicData.reset();

    // load the cache with the order
    Order order1 = Ebean.find(Order.class, 1L);

    // load the Collection IDs (order.orderDetail) cache
    OrderDetail orderDetail1 = order1.getDetails().get(0);

    // delete one order detail from DB. This triggers clearing of OrderDetail caches
    // and should also clear any Collection IDs caches targeting OrderDetail bean
    String updStatement = "delete from orderDetail where id = :id";
    Update<OrderDetail> update = Ebean.createUpdate(OrderDetail.class, updStatement);
    update.set("id", orderDetail1.getId());
    int rows = update.execute();
    assertEquals(1, rows);

    // read the order from cache
    Order orderFromCache = Ebean.find(Order.class, 1L);
    OrderDetail orderDetailFromCache = orderFromCache.getDetails().get(0);

    // trigger reading from the DB
    orderDetailFromCache.getCretime();

    // if we got here then OK
  }


  /**
   * When doing an external modification on a table the collection cache must be cleared.
   * This is true for all changes insert,update, delete. Tested for delete here.
   */
  @Test
  @Ignore
  public void testClearingCollectionCacheOnExternalModification() {
    // arrange
    ResetBasicData.reset();

    // load the cache with the order
    Order order1 = Ebean.find(Order.class, 1L);

    // load the Collection IDs (order.orderDetail) cache
    OrderDetail orderDetail1 = order1.getDetails().get(0);

    // delete one order detail from DB using native SQL. This triggers clearing of OrderDetail caches
    // and should also clear any Collection IDs caches targeting OrderDetail bean
    String updStatement = "delete from o_order_detail where id = :id";
    SqlUpdate update = Ebean.createSqlUpdate(updStatement);
    update.setParameter("id", orderDetail1.getId());
    int rows = update.execute();
    assertEquals(1, rows);

    // We need to notify the cache manually
    Ebean.externalModification("o_order_detail", false, false, true);

    // read the order from cache
    Order orderFromCache = Ebean.find(Order.class, 1L);
    OrderDetail orderDetailFromCache = orderFromCache.getDetails().get(0);

    // trigger reading the whole bean
    orderDetailFromCache.getCretime();

    // if we got here then OK
  }

}
