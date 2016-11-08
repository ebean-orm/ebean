package com.avaje.tests.rawsql;

import com.avaje.ebean.*;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestRawSqlOrmQueryDistinct extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    RawSql rawSql0 = RawSqlBuilder.parse(" select  distinct  r.id, r.name from o_customer r ")
      .create();

    Query<Customer> query0 = Ebean.find(Customer.class);
    query0.setRawSql(rawSql0);
    query0.where().ilike("name", "r%");

    RawSql rawSql = RawSqlBuilder.parse(" select  distinct  r.id, r.name from o_customer r ")
      .columnMapping("r.id", "id").columnMapping("r.name", "name").create();

    Query<Customer> query = Ebean.find(Customer.class);
    query.setRawSql(rawSql);
    query.where().ilike("name", "r%");

    query.fetch("contacts", new FetchConfig().query());
    query.filterMany("contacts").gt("lastName", "b");

    List<Customer> list = query.findList();
    Assert.assertNotNull(list);
  }

}
