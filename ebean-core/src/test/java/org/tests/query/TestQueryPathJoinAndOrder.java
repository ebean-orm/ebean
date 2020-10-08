package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestQueryPathJoinAndOrder extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    List<Customer> list = Ebean.find(Customer.class).select("id,name, status").fetch("contacts")
      .order().asc("id").order().desc("contacts.firstName").setMaxRows(3).findList();

    Assert.assertNotNull(list);

    // can't really assert that the contacts are batch loaded
    // via a secondary query join

  }
}
