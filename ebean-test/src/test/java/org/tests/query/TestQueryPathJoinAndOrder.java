package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestQueryPathJoinAndOrder extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    List<Customer> list = DB.find(Customer.class).select("id,name, status").fetch("contacts")
      .order().asc("id").order().desc("contacts.firstName").setMaxRows(3).findList();

    assertNotNull(list);

    // can't really assert that the contacts are batch loaded
    // via a secondary query join

  }
}
