package org.tests.query.orderby;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

public class TestOrderByWithFunction extends BaseTestCase {

  @Test
  public void testWithFunction() {

    ResetBasicData.reset();
    String length = "length";
    if (isSqlServer()) {
      length = "len";
    }
    
    Query<Customer> query = Ebean.find(Customer.class).order(length + "(name),name");

    query.findList();
    String sql = query.getGeneratedSql();

    Assert.assertTrue(sql.contains("order by " + length + "(t0.name)"));
  }
}
