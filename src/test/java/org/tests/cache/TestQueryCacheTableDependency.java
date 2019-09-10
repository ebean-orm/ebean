package org.tests.cache;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Ebean;
import io.ebean.Query;
import io.ebean.cache.ServerCache;

import org.junit.Test;
import org.tests.model.basic.Address;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;
import org.tests.model.basic.cache.ECacheChild;
import org.tests.model.basic.cache.ECacheRoot;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestQueryCacheTableDependency extends BaseTestCase {

  @Test
  public void testFindCountOnDependent() {

    ResetBasicData.reset();

    ServerCache customerCache = Ebean.getServerCacheManager().getQueryCache(Customer.class);
    customerCache.clear();

    List<Address> addrs = Ebean.find(Address.class)
      .where().eq("line2", "St Lukes")
      .findList();

    int custs = Ebean.find(Customer.class).setUseQueryCache(true).setReadOnly(true)
      .where().eq("billingAddress.line2", "St Lukes")
      .findCount();

    assertThat(custs).isEqualTo(3);

    custs = Ebean.find(Customer.class).setUseQueryCache(true).setReadOnly(true)
      .where().eq("billingAddress.line2", "St Lukes")
      .findCount();

    assertThat(custs).isEqualTo(3);

    Address a1 = addrs.get(0);
    a1.setLine2("St Lucky");
    Ebean.save(a1);

    custs = Ebean.find(Customer.class).setUseQueryCache(true).setReadOnly(true)
      .where().eq("billingAddress.line2", "St Lukes")
      .findCount();

    assertThat(custs).isEqualTo(2);


    custs = Ebean.find(Customer.class).setUseQueryCache(true).setReadOnly(true)
      .where().eq("billingAddress.line2", "St Lucky")
      .findCount();

    assertThat(custs).isEqualTo(1);

    Ebean.update(Address.class)
      .set("line2", "St Lucky2")
      .where().eq("line2", "St Lucky")
      .update();

    custs = Ebean.find(Customer.class).setUseQueryCache(true).setReadOnly(true)
      .where().eq("billingAddress.line2", "St Lucky")
      .findCount();

    assertThat(custs).isEqualTo(0);

    custs = Ebean.find(Customer.class).setUseQueryCache(true).setReadOnly(true)
      .where().eq("billingAddress.line2", "St Lucky2")
      .findCount();
    assertThat(custs).isEqualTo(1);

    Ebean.createSqlUpdate("update o_address set line_2=? where line_2=?")
      .setNextParameter("St Lucky3")
      .setNextParameter("St Lucky2")
      .execute();

    custs = Ebean.find(Customer.class).setUseQueryCache(true).setReadOnly(true)
      .where().eq("billingAddress.line2", "St Lucky2")
      .findCount();
    assertThat(custs).isEqualTo(0);

    custs = Ebean.find(Customer.class).setUseQueryCache(true).setReadOnly(true)
      .where().eq("billingAddress.line2", "St Lucky3")
      .findCount();

    assertThat(custs).isEqualTo(1);

  }

  @Test
  public void testFindCountOnOtherL2Cached() {

    ResetBasicData.reset();

    Customer fi = Ebean.find(Customer.class).where().eq("name", "Fiona").findOne();

    int custCount0 = Ebean.find(Customer.class).setUseQueryCache(true).setReadOnly(true)
      .where()
      .eq("name", "Fiona")
      .isNull("contacts.phone")
      .findCount();

    assertThat(custCount0).isEqualTo(1);

    int updateRows = Ebean.update(Contact.class)
      .set("phone", "1234")
      .where()
      .eq("customer.id", fi.getId())
      .update();

    assertThat(updateRows).isGreaterThan(0);

    int custCount1 = Ebean.find(Customer.class).setUseQueryCache(true).setReadOnly(true)
      .where()
      .eq("name", "Fiona")
      .isNull("contacts.phone")
      .findCount();

    assertThat(custCount1).isEqualTo(0);

  }

  @Test
  public void testCache_withInvalidateQueryCache() throws InterruptedException {
    // So Address has no query cache enabled, but has @InvalidateQueryCache annotation
    Address root = new Address();
    root.setCity("new york");
    DB.save(root);

    Customer child = new Customer();
    child.setName("bobby");
    child.setBillingAddress(root);
    DB.save(child);
    DB.getServerCacheManager().getQueryCache(Address.class).clear();
    DB.getServerCacheManager().getQueryCache(Customer.class).clear();
    // Test preparation finished, start the test
    Thread.sleep(10);

    Query<Customer> query = DB.find(Customer.class).where().eq("billingAddress.city", "new york").query();
    query.setUseQueryCache(true);

    assertThat(query.findCount()).isEqualTo(1);

    root.setCity("san francisco");
    DB.save(root);

    assertThat(query.findCount()).isEqualTo(0);

    // clean up
    DB.delete(child);
    // DB.delete(root); deleted by M2O cascade.
  }

  @Test
  public void testCache_withEnabeldQueryCache() throws InterruptedException {
    // ECacheRoot and ECacheChild have enabled query cache.
    ECacheRoot root = new ECacheRoot();
    root.setName("testRoot");
    DB.save(root);

    ECacheChild child = new ECacheChild();
    child.setName("testChild");
    child.setRoot(root);
    DB.save(child);
    DB.getServerCacheManager().getQueryCache(ECacheChild.class).clear();
    DB.getServerCacheManager().getQueryCache(ECacheRoot.class).clear();
    // Test preparation finished, start the test
    Thread.sleep(10);
    // we need this thread.sleep here, because on a fast machine, DB.save(child) and computing the
    // queryCacheEntry will happen in the same millisecond (or 10 milliseconds, which is the resolution
    // of System.currentTimeMillis() on windows 7 & java 8)
    Query<ECacheChild> query = DB.find(ECacheChild.class).where().eq("root.name", "testRoot").query();
    query.setUseQueryCache(true);

    assertThat(query.findCount()).isEqualTo(1);

    root = DB.find(ECacheRoot.class).findList().get(0);
    root.setName("test2");
    DB.save(root);

    assertThat(query.findCount()).isEqualTo(0);

    // clean up
    DB.delete(child);
    DB.delete(root);
  }
}
