package org.tests.rawsql;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestRawSqlUnparsedQuery extends BaseTestCase {

  @Test
  public void testDoubleUnparsedQuery() {

    // Checks for RawSql caching issue https://github.com/ebean-orm/avaje-ebeanorm/issues/259
    ResetBasicData.reset();

    test();
    test();
  }

  private static void test() {
    RawSql rawSql = RawSqlBuilder
      .unparsed("select r.id, r.name from o_customer r where r.id >= :a and r.name like :b")
      .columnMapping("r.id", "id").columnMapping("r.name", "name").create();

    Query<Customer> query = Ebean.find(Customer.class);
    query.setRawSql(rawSql);
    query.setParameter("a", 1);
    query.setParameter("b", "R%");

    List<Customer> list = query.findList();
    Assert.assertNotNull(list);
  }

}
