package com.avaje.tests.rawsql;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import com.avaje.tests.model.basic.CustomerAggregate;
import com.avaje.tests.model.basic.ResetBasicData;
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
