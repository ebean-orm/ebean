package org.tests.insert;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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

    DB.insertAll(customers);

    assertNotNull(cust1.getId());
    assertNotNull(cust2.getId());

    Customer cust1Check = DB.find(Customer.class, cust1.getId());
    assertEquals(cust1.getName(), cust1Check.getName());
    Customer cust2Check = DB.find(Customer.class, cust2.getId());
    assertEquals(cust2.getName(), cust2Check.getName());

    cust1.setName("jim-changed");
    cust2.setName("bob-changed");

    DB.updateAll(customers);
    awaitL2Cache();

    Customer cust1Check2 = DB.find(Customer.class, cust1.getId());
    assertEquals("jim-changed", cust1Check2.getName());
    Customer cust2Check2 = DB.find(Customer.class, cust2.getId());
    assertEquals("bob-changed", cust2Check2.getName());


    cust1Check2.setName("jim-updated");
    Customer cust3 = new Customer();
    cust3.setName("mac");

    List<Customer> saveList = new ArrayList<>();
    saveList.add(cust1Check2);
    saveList.add(cust3);

    DB.saveAll(saveList);
    awaitL2Cache();


    Customer cust1Check3 = DB.find(Customer.class, cust1.getId());
    assertEquals("jim-updated", cust1Check3.getName());
    Customer cust3Check = DB.find(Customer.class, cust3.getId());
    assertEquals("mac", cust3Check.getName());

    List<Customer> deleteList = new ArrayList<>();
    deleteList.add(cust1Check3);
    deleteList.add(cust3);
    deleteList.add(cust2Check2);

    DB.deleteAll(deleteList);
    awaitL2Cache();

    assertNull(DB.find(Customer.class, cust1Check3.getId()));
    assertNull(DB.find(Customer.class, cust2Check2.getId()));
    assertNull(DB.find(Customer.class, cust3.getId()));
  }

}
