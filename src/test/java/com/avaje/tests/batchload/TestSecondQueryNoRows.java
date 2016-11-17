package com.avaje.tests.batchload;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.FetchConfig;
import com.avaje.tests.model.basic.Customer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestSecondQueryNoRows extends BaseTestCase {

  @Test
  public void test() {

    Customer cnew = new Customer();
    cnew.setName("testSecQueryNoRows");

    Ebean.save(cnew);

    Customer c = Ebean.find(Customer.class)
      .setAutoTune(false)
      .setId(cnew.getId())
      .fetch("contacts", new FetchConfig().query())
      .findUnique();

    assertNotNull(c);
    assertEquals(0, c.getContacts().size());
  }
}
