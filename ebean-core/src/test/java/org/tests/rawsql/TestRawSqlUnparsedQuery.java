package org.tests.rawsql;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;
import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestRawSqlUnparsedQuery extends BaseTestCase {

  private static final RawSql rawSql = RawSqlBuilder
    .unparsed("select r.id, r.name from o_customer r where r.id >= :a and r.name like :b")
    .columnMapping("r.id", "id")
    .columnMapping("r.name", "name")
    .create();

  @Test
  public void testDoubleUnparsedQuery() {

    // Checks for RawSql caching issue https://github.com/ebean-orm/avaje-ebeanorm/issues/259
    ResetBasicData.reset();

    test();
    test();
  }

  private static void test() {

    List<Customer> list = DB.find(Customer.class)
      .setRawSql(rawSql)
      .setParameter("a", 1)
      .setParameter("b", "R%")
      .findList();

    assertThat(list).isNotNull();
  }

}
