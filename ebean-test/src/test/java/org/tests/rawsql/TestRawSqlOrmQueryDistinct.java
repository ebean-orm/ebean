package org.tests.rawsql;

import io.ebean.*;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestRawSqlOrmQueryDistinct extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    RawSql rawSql0 = RawSqlBuilder.parse(" select  distinct  r.id, r.name from o_customer r ")
      .create();

    Query<Customer> query0 = DB.find(Customer.class);
    query0.setRawSql(rawSql0);
    query0.where().ilike("name", "r%");

    RawSql rawSql = RawSqlBuilder.parse(" select  distinct  r.id, r.name from o_customer r ")
      .columnMapping("r.id", "id").columnMapping("r.name", "name").create();

    Query<Customer> query = DB.find(Customer.class);
    query.setRawSql(rawSql);
    query.where().ilike("name", "r%");

    query.fetchQuery("contacts");
    query.filterMany("contacts").gt("lastName", "b");

    List<Customer> list = query.findList();
    assertNotNull(list);
  }

}
