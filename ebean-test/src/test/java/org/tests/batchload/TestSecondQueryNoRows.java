package org.tests.batchload;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestSecondQueryNoRows extends BaseTestCase {

  @BeforeAll
  static void before() {
    ResetBasicData.reset();
  }

  @Test
  public void test() {
    Customer cnew = new Customer();
    cnew.setName("testSecQueryNoRows");

    DB.save(cnew);

    Customer c = DB.find(Customer.class)
      .setAutoTune(false)
      .setId(cnew.getId())
      .fetchQuery("contacts")
      .findOne();

    assertNotNull(c);
    assertEquals(0, c.getContacts().size());

    DB.delete(c);
  }
}
