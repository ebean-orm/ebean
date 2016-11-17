package com.avaje.tests.autofetch;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.tests.model.basic.Address;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;
import org.avaje.ebeantest.LoggedSqlCollector;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Set;

public class TunedQueryWithNullFetchedBeanTest extends BaseTestCase {

  EbeanServer server = Ebean.getServer(null);

  @Test
  public void withFetchOfNullBeanJoin() {

    ResetBasicData.reset();

    Customer newCustomer = new Customer();
    newCustomer.setName("TestFetchBillingAddress");
    server.save(newCustomer);

    Query<Customer> query = server.find(Customer.class)
      .setId(newCustomer.getId())
      .fetch("billingAddress", "id");

    LoggedSqlCollector.start();

    Customer customer = query.findUnique();
    EntityBean eb = (EntityBean) customer;
    EntityBeanIntercept ebi = eb._ebean_getIntercept();

    Assert.assertTrue(ebi.isFullyLoadedBean());

    // find the internal property index for "billingAddress"
    String[] propNames = eb._ebean_getPropertyNames();
    int pos = 0;
    for (int i = 0; i < propNames.length; i++) {
      if (propNames[i].equals("billingAddress")) {
        pos = i;
      }
    }

    // The billing address is loaded (but value null)
    Assert.assertTrue(ebi.isLoadedProperty(pos));

    Set<String> loadedPropertyNames = ebi.getLoadedPropertyNames();
    Assert.assertNull(loadedPropertyNames);

    // no lazy loading expected here, value is null
    Address billingAddress = customer.getBillingAddress();
    Assert.assertNull(billingAddress);

    // assert only one query executed
    List<String> loggedSql = LoggedSqlCollector.stop();
    Assert.assertEquals(1, loggedSql.size());
  }
}
