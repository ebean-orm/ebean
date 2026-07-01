package org.tests.cache;

import io.ebean.*;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheManager;
import io.ebean.xtest.BaseTestCase;
import io.ebeaninternal.server.cache.CachedManyIds;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TestCacheCollectionIds extends BaseTestCase {

  private final ServerCacheManager cacheManager = DB.cacheManager();

  @Test
  void test() {
    ResetBasicData.reset();
    awaitL2Cache();

    ServerCache custCache = cacheManager.beanCache(Customer.class);
    ServerCache custManyIdsCache = cacheManager.collectionIdsCache(Customer.class, "contacts");

    // cacheManager.setCaching(Customer.class, true);
    // cacheManager.setCaching(Contact.class, true);

    custCache.clear();
    custManyIdsCache.clear();

    List<Customer> list = DB.find(Customer.class).setAutoTune(false).setBeanCacheMode(CacheMode.PUT)
      .orderBy().asc("id").findList();

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

    DB.save(newContact);
    awaitL2Cache();

    int currentNumContacts2 = fetchCustomer(customer.getId());
    assertEquals(currentNumContacts + 1, currentNumContacts2);

    // cleanup
    DB.delete(newContact);
  }

  private int fetchCustomer(Integer id) {
    Customer customer2 = DB.find(Customer.class, id);

    List<Contact> contacts2 = customer2.getContacts();

    for (Contact contact : contacts2) {
      assertNotNull(contact.getFirstName());
      contact.getEmail();
    }
    return contacts2.size();
  }

  /**
   * When updating a ManyToMany relations also the collection cache must be updated.
   */
  @Test
  void testUpdatingCollectionCacheForManyToManyRelations() {
    // arrange
    ResetBasicData.reset();

    OCachedBean cachedBean = new OCachedBean();
    cachedBean.setName("hello");
    cachedBean.getCountries().add(DB.find(Country.class, "NZ"));
    cachedBean.getCountries().add(DB.find(Country.class, "AU"));

    DB.save(cachedBean);

    // used to just load the cache - trigger loading
    OCachedBean dummyToLoad = DB.find(OCachedBean.class, cachedBean.getId());
    dummyToLoad.getCountries().size();

    ServerCache cachedBeanCountriesCache = cacheManager.collectionIdsCache(OCachedBean.class, "countries");
    CachedManyIds cachedManyIds = (CachedManyIds) cachedBeanCountriesCache.get(String.valueOf(cachedBean.getId()));

    // confirm the starting data and cache entry
    assertEquals(2, dummyToLoad.getCountries().size());
    assertEquals(2, cachedManyIds.getIdList().size());


    // act
    OCachedBean loadedBean = DB.find(OCachedBean.class, cachedBean.getId());
    loadedBean.getCountries().clear();
    loadedBean.getCountries().add(DB.find(Country.class, "AU"));

    DB.save(loadedBean);
    awaitL2Cache();

    // Get the data to assert/check against
    OCachedBean result = DB.find(OCachedBean.class, cachedBean.getId());
    cachedManyIds = (CachedManyIds) cachedBeanCountriesCache.get(String.valueOf(result.getId()));

    // assert that data and cache both show correct data
    assertEquals(1, result.getCountries().size());
    assertEquals(1, cachedManyIds.getIdList().size());
    assertFalse(cachedManyIds.getIdList().contains("NZ"));
    assertTrue(cachedManyIds.getIdList().contains("AU"));
  }

  @Test
  void testChangingCollectionByEditingConnectedCachedBean() {
    ResetBasicData.reset();

    OCachedBean cachedBean = new OCachedBean();
    cachedBean.setName("hello1");

    DB.save(cachedBean);
    awaitL2Cache();

    OCachedBean dummyToLoad = DB.find(OCachedBean.class, cachedBean.getId());
    assertEquals(0, dummyToLoad.getChildren().size());

    OCachedBeanChild child1=new OCachedBeanChild();
    child1.setCachedBean(cachedBean);
    DB.insert(child1);
    awaitL2Cache();
    dummyToLoad = DB.find(OCachedBean.class, cachedBean.getId());
    assertEquals(1, dummyToLoad.getChildren().size());

    OCachedBeanChild child2=new OCachedBeanChild();
    child2.setCachedBean(cachedBean);
    DB.insert(child2);
    awaitL2Cache();
    dummyToLoad = DB.find(OCachedBean.class, cachedBean.getId());
    assertEquals(2, dummyToLoad.getChildren().size());

    DB.delete(child2);
    awaitL2Cache();
    dummyToLoad = DB.find(OCachedBean.class, cachedBean.getId());
    assertEquals(1, dummyToLoad.getChildren().size());

    DB.delete(child1);
    awaitL2Cache();
    dummyToLoad = DB.find(OCachedBean.class, cachedBean.getId());
    assertEquals(0, dummyToLoad.getChildren().size());

    DB.delete(cachedBean);
  }

  @Test
  void testChangingCollectionByEditingConnectedNotCachedBean() {
    ResetBasicData.reset();

    OCachedBean cachedBean = new OCachedBean();
    cachedBean.setName("hello2");

    DB.save(cachedBean);
    awaitL2Cache();

    OCachedBean dummyToLoad = DB.find(OCachedBean.class, cachedBean.getId());
    assertEquals(0, dummyToLoad.getNotCachedChildren().size());

    OBeanChild child1=new OBeanChild();
    child1.setCachedBean(cachedBean);
    DB.insert(child1);
    awaitL2Cache();
    dummyToLoad = DB.find(OCachedBean.class, cachedBean.getId());
    assertEquals(1, dummyToLoad.getNotCachedChildren().size());

    OBeanChild child2=new OBeanChild();
    child2.setCachedBean(cachedBean);
    DB.insert(child2);
    awaitL2Cache();
    dummyToLoad = DB.find(OCachedBean.class, cachedBean.getId());
    assertEquals(2, dummyToLoad.getNotCachedChildren().size());

    DB.delete(child2);
    awaitL2Cache();
    dummyToLoad = DB.find(OCachedBean.class, cachedBean.getId());
    assertEquals(1, dummyToLoad.getNotCachedChildren().size());

    DB.delete(child1);
    awaitL2Cache();
    dummyToLoad = DB.find(OCachedBean.class, cachedBean.getId());
    assertEquals(0, dummyToLoad.getNotCachedChildren().size());

    DB.delete(cachedBean);
  }

  /**
   * When updating a ManyToMany relations also the collection cache must be updated.
   * Alternate to above test where in this case the bean is dirty - loadedBean.setName("goodbye");.
   */
  @Test
  void testUpdatingCollectionCacheForManyToManyRelationsWithUpdatedBean() {
    // arrange
    ResetBasicData.reset();

    OCachedBean cachedBean = new OCachedBean();
    cachedBean.setName("hello");
    cachedBean.getCountries().add(DB.find(Country.class, "NZ"));
    cachedBean.getCountries().add(DB.find(Country.class, "AU"));

    DB.save(cachedBean);

    // used to just load the cache - trigger loading
    OCachedBean dummyToLoad = DB.find(OCachedBean.class, cachedBean.getId());
    dummyToLoad.getCountries().size();

    ServerCache cachedBeanCountriesCache = cacheManager.collectionIdsCache(OCachedBean.class, "countries");
    CachedManyIds cachedManyIds = (CachedManyIds) cachedBeanCountriesCache.get(String.valueOf(cachedBean.getId()));

    // confirm the starting data and cache entry
    assertEquals(2, dummyToLoad.getCountries().size());
    assertEquals(2, cachedManyIds.getIdList().size());


    // act - this time update the name property so the bean is dirty
    OCachedBean loadedBean = DB.find(OCachedBean.class, cachedBean.getId());
    loadedBean.setName("goodbye");
    loadedBean.getCountries().clear();
    loadedBean.getCountries().add(DB.find(Country.class, "AU"));

    DB.save(loadedBean);
    awaitL2Cache();

    // Get the data to assert/check against
    OCachedBean result = DB.find(OCachedBean.class, cachedBean.getId());
    cachedManyIds = (CachedManyIds) cachedBeanCountriesCache.get(String.valueOf(result.getId()));

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
  void testUpdatingCollectionCacheForManyToManyRelationsWithinStatelessUpdate() {
    // arrange
    ResetBasicData.reset();

    OCachedBean cachedBean = new OCachedBean();
    cachedBean.setName("cachedBeanTest");
    cachedBean.getCountries().add(DB.find(Country.class, "NZ"));
    cachedBean.getCountries().add(DB.find(Country.class, "AU"));

    DB.save(cachedBean);

    // clear the cache
    ServerCache cachedBeanCountriesCache = cacheManager.collectionIdsCache(OCachedBean.class, "countries");
    cachedBeanCountriesCache.clear();
    assertEquals(0, cachedBeanCountriesCache.size());

    // load the cache
    OCachedBean dummyLoad = DB.find(OCachedBean.class, cachedBean.getId());
    List<Country> dummyCountries = dummyLoad.getCountries();
    assertEquals(2, dummyCountries.size());

    // assert that the cache contains the expected entry
    assertEquals(1, cachedBeanCountriesCache.size());
    CachedManyIds dummyEntry = (CachedManyIds) cachedBeanCountriesCache.get(String.valueOf(dummyLoad.getId()));
    assertNotNull(dummyEntry);
    assertEquals(2, dummyEntry.getIdList().size());
    assertTrue(dummyEntry.getIdList().contains("NZ"));
    assertTrue(dummyEntry.getIdList().contains("AU"));


    // act - this should invalidate our cache entry
    OCachedBean update = new OCachedBean();
    update.setId(cachedBean.getId());
    update.setName("modified");
    update.getCountries().add(DB.find(Country.class, "AU"));

    DB.update(update);
    awaitL2Cache();

    assertEquals(1, cachedBeanCountriesCache.size());

    CachedManyIds cachedManyIds = (CachedManyIds) cachedBeanCountriesCache.get(String.valueOf(update.getId()));

    // assert cache updated
    assertEquals(1, cachedManyIds.getIdList().size());
    assertFalse(cachedManyIds.getIdList().contains("NZ"));
    assertTrue(cachedManyIds.getIdList().contains("AU"));

    // assert countries good
    OCachedBean result = DB.find(OCachedBean.class, cachedBean.getId());
    assertEquals(1, result.getCountries().size());

  }

  @Test
  void testUpdating_noChange() throws InterruptedException {
    ResetBasicData.reset();

    try (Transaction txn = DB.beginTransaction()) {

      OCachedBean cachedBean = new OCachedBean();
      cachedBean.setName("helloForUpdate");
      DB.save(cachedBean);

      cachedBean = DB.find(OCachedBean.class, cachedBean.getId());

      cachedBean.setName("mod");
      ArrayList<Country> list = new ArrayList<>();
      list.add(DB.find(Country.class, "NZ"));
      cachedBean.setCountries(list);
      DB.save(cachedBean);


      cachedBean.setName("mod2");
      DB.save(cachedBean);

      txn.commit();
    }


    Thread.sleep(2000);
  }

  /**
   * When doing an ORM update the collection cache must be cleared.
   */
  @Test
  @Disabled
  void testClearingCollectionCacheOnORMUpdate() {
    // arrange
    ResetBasicData.reset();

    // load the cache with the order
    Order order1 = DB.find(Order.class, 1L);

    // load the Collection IDs (order.orderDetail) cache
    OrderDetail orderDetail1 = order1.getDetails().get(0);

    // delete one order detail from DB. This triggers clearing of OrderDetail caches
    // and should also clear any Collection IDs caches targeting OrderDetail bean
    String updStatement = "delete from orderDetail where id = :id";
    Update<OrderDetail> update = DB.createUpdate(OrderDetail.class, updStatement);
    update.set("id", orderDetail1.getId());
    int rows = update.execute();
    assertEquals(1, rows);

    // read the order from cache
    Order orderFromCache = DB.find(Order.class, 1L);
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
  @Disabled
  void testClearingCollectionCacheOnExternalModification() {
    // arrange
    ResetBasicData.reset();

    // load the cache with the order
    Order order1 = DB.find(Order.class, 1L);

    // load the Collection IDs (order.orderDetail) cache
    OrderDetail orderDetail1 = order1.getDetails().get(0);

    // delete one order detail from DB using native SQL. This triggers clearing of OrderDetail caches
    // and should also clear any Collection IDs caches targeting OrderDetail bean
    String updStatement = "delete from o_order_detail where id = :id";
    SqlUpdate update = DB.sqlUpdate(updStatement);
    update.setParameter("id", orderDetail1.getId());
    int rows = update.execute();
    assertEquals(1, rows);

    // We need to notify the cache manually
    DB.externalModification("o_order_detail", false, false, true);

    // read the order from cache
    Order orderFromCache = DB.find(Order.class, 1L);
    OrderDetail orderDetailFromCache = orderFromCache.getDetails().get(0);

    // trigger reading the whole bean
    orderDetailFromCache.getCretime();

    // if we got here then OK
  }

}
