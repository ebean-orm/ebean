package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestLimitQuery extends BaseTestCase {

  @Test
  public void testHasManyWithLimit() {

    ResetBasicData.reset();

    List<Customer> customers = Ebean.find(Customer.class)
      .setAutoTune(false)
      .setFirstRow(0)
      .setMaxRows(10)
      .where().like("name", "%A%")
      .findList();

    // should at least find the "Cust NoAddress" customer
    Assert.assertTrue(!customers.isEmpty());

  }
}
