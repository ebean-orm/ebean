package org.tests.rawsql;

import io.ebean.BaseTestCase;
import io.ebean.CallableSql;
import io.ebean.Ebean;
import io.ebean.Query;
import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;
import io.ebean.Transaction;
import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;

import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

  @Test
  @ForPlatform(Platform.MYSQL)
  public void testMySqlColonEquals() throws SQLException {

    ResetBasicData.reset();

    Transaction transaction = Ebean.beginTransaction();

    try {
      if ("MariaDB connector/J".equals(transaction.getConnection().getMetaData().getDriverName())) {
        return; // MariaDb only supports callable statements in the form "? = call function x(?)"
      }
      CallableSql callableSql = Ebean.createCallableSql("set @total = 0");
      Ebean.execute(callableSql);

      String sql = "select id, @total := 0 + id as total_items from o_order";

      Query<Order> query1 = Ebean.findNative(Order.class, sql);
      List<Order> list = query1.findList();

      assertThat(list.get(0).getTotalItems()).isNotNull();

      Ebean.execute(callableSql);

      RawSql rawSql = RawSqlBuilder
        .parse(sql)
        .create();

      Query<Order> query = Ebean.find(Order.class)
        .setRawSql(rawSql);

      List<Order> list1 = query.findList();
      assertThat(list1.get(0).getTotalItems()).isNotNull();

    } finally {
      transaction.end();
    }
  }
}
