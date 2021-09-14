package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestLimitQuery extends BaseTestCase {

  @Test
  public void testHasManyWithLimit() {

    ResetBasicData.reset();

    List<Customer> customers = DB.find(Customer.class)
      .setAutoTune(false)
      .setFirstRow(0)
      .setMaxRows(10)
      .where().like("name", "%A%")
      .findList();

    // should at least find the "Cust NoAddress" customer
    assertTrue(!customers.isEmpty());

  }
}
