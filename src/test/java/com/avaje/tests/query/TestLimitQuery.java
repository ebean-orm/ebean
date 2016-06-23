package com.avaje.tests.query;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;
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