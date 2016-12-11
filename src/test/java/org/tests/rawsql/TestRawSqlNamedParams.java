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

public class TestRawSqlNamedParams extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    RawSql rawSql = RawSqlBuilder
      .parse("select r.id, r.name from o_customer r where r.id > :id and r.name like :name")
      .columnMapping("r.id", "id").columnMapping("r.name", "name").create();

    Query<Customer> query = Ebean.find(Customer.class);
    query.setRawSql(rawSql);
    query.setParameter("name", "R%");
    query.setParameter("id", 0);
    query.where().lt("id", 1000);

    List<Customer> list = query.findList();

    Assert.assertNotNull(list);
  }
}
