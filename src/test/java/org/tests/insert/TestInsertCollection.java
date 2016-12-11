package org.tests.insert;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.Customer;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestInsertCollection extends BaseTestCase {

  @Test
  public void test() {

    Customer cust1 = new Customer();
    cust1.setName("jim");

    Customer cust2 = new Customer();
    cust2.setName("bob");

    List<Customer> customers = new ArrayList<>();
    customers.add(cust1);
    customers.add(cust2);

    Ebean.insertAll(customers);

    Assert.assertNotNull(cust1.getId());
    Assert.assertNotNull(cust2.getId());

    Customer cust1Check = Ebean.find(Customer.class, cust1.getId());
    Assert.assertEquals(cust1.getName(), cust1Check.getName());
    Customer cust2Check = Ebean.find(Customer.class, cust2.getId());
    Assert.assertEquals(cust2.getName(), cust2Check.getName());

    cust1.setName("jim-changed");
    cust2.setName("bob-changed");

    Ebean.updateAll(customers);
    awaitL2Cache();

    Customer cust1Check2 = Ebean.find(Customer.class, cust1.getId());
    Assert.assertEquals("jim-changed", cust1Check2.getName());
    Customer cust2Check2 = Ebean.find(Customer.class, cust2.getId());
    Assert.assertEquals("bob-changed", cust2Check2.getName());


    cust1Check2.setName("jim-updated");
    Customer cust3 = new Customer();
    cust3.setName("mac");

    List<Customer> saveList = new ArrayList<>();
    saveList.add(cust1Check2);
    saveList.add(cust3);

    Ebean.saveAll(saveList);
    awaitL2Cache();


    Customer cust1Check3 = Ebean.find(Customer.class, cust1.getId());
    Assert.assertEquals("jim-updated", cust1Check3.getName());
    Customer cust3Check = Ebean.find(Customer.class, cust3.getId());
    Assert.assertEquals("mac", cust3Check.getName());

    List<Customer> deleteList = new ArrayList<>();
    deleteList.add(cust1Check3);
    deleteList.add(cust3);
    deleteList.add(cust2Check2);

    Ebean.deleteAll(deleteList);
    awaitL2Cache();

    Assert.assertNull(Ebean.find(Customer.class, cust1Check3.getId()));
    Assert.assertNull(Ebean.find(Customer.class, cust2Check2.getId()));
    Assert.assertNull(Ebean.find(Customer.class, cust3.getId()));

  }


}
