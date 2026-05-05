package org.tests.query.orderby;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestOrderByWithFunction extends BaseTestCase {

  @Test
  public void testWithFunction() {

    ResetBasicData.reset();
    String length = "length";
    if (isSqlServer()) {
      length = "len";
    }

    Query<Customer> query = DB.find(Customer.class).orderBy(length + "(name),name");

    query.findList();
    String sql = query.getGeneratedSql();

    assertTrue(sql.contains("order by " + length + "(t0.name)"));
  }
}
