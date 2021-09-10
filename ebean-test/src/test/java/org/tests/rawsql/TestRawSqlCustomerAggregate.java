package org.tests.rawsql;

import io.ebean.*;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.CustomerAggregate;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestRawSqlCustomerAggregate extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    RawSql rawSql = RawSqlBuilder
      .parse(
        "select c.customer_id, count(*) as totalContacts from contact c  group by c.customer_id")
      .columnMapping("c.customer_id", "customer.id").create();

    Query<CustomerAggregate> query = DB.find(CustomerAggregate.class);
    query.setRawSql(rawSql);
    query.where().ge("customer.id", 1);

    List<CustomerAggregate> list = query.findList();
    assertNotNull(list);
  }
}
