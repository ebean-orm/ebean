package org.tests.batchload;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestSecondQueryNoRows extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

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
