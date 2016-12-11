package org.tests.rawsql;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertNotNull;

public class TestRawSqlPositionedParams extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    RawSql rawSql = RawSqlBuilder
      .parse("select r.id, r.name from o_customer r where r.id >= ? and r.name like ?")
      .create();

    Query<Customer> query = Ebean.find(Customer.class);
    query.setRawSql(rawSql);
    query.setParameter(1, 1);
    query.setParameter(2, "R%");
    query.where().lt("id", 2001);

    List<Customer> list = query.findList();

    assertNotNull(list);
  }

  @Test
  public void test_unparsed() {

    ResetBasicData.reset();

    RawSql rawSql = RawSqlBuilder
      .unparsed("select r.id, r.name from o_customer r where r.id >= ? and r.name like ?")
      .columnMapping("r.id", "id")
      .columnMapping("r.name", "name").create();

    Query<Customer> query = Ebean.find(Customer.class);
    query.setRawSql(rawSql);
    query.setParameter(1, 1);
    query.setParameter(2, "R%");

    List<Customer> list = query.findList();

    assertNotNull(list);
  }
}
