package org.tests.rawsql;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;
import org.tests.model.basic.CustomerAggregate;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestRawSqlCustomerAggregate extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    RawSql rawSql = RawSqlBuilder
      .parse(
        "select c.customer_id, count(*) as totalContacts from contact c  group by c.customer_id")
      .columnMapping("c.customer_id", "customer.id").create();

    Query<CustomerAggregate> query = Ebean.find(CustomerAggregate.class);
    query.setRawSql(rawSql);
    query.where().ge("customer.id", 1);

    List<CustomerAggregate> list = query.findList();
    Assert.assertNotNull(list);
  }
}
