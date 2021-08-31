package org.tests.batchload;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.FetchConfig;
import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestSecondQueryNoRows extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Customer cnew = new Customer();
    cnew.setName("testSecQueryNoRows");

    Ebean.save(cnew);

    Customer c = Ebean.find(Customer.class)
      .setAutoTune(false)
      .setId(cnew.getId())
      .fetchQuery("contacts")
      .findOne();

    assertNotNull(c);
    assertEquals(0, c.getContacts().size());

    Ebean.delete(c);
  }
}
