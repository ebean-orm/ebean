package org.tests.query;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestQueryPathJoinAndOrder extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    List<Customer> list = DB.find(Customer.class).select("id,name, status").fetch("contacts")
      .orderBy().asc("id").orderBy().desc("contacts.firstName").setMaxRows(3).findList();

    assertNotNull(list);

    // can't really assert that the contacts are batch loaded
    // via a secondary query join

  }
}
