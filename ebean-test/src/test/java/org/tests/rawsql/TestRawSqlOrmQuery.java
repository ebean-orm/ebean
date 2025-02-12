package org.tests.rawsql;

import io.ebean.*;
import io.ebean.annotation.Platform;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import io.ebean.xtest.IgnorePlatform;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.Phone;
import org.tests.model.basic.ResetBasicData;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TestRawSqlOrmQuery extends BaseTestCase {

  @Test
  void test() {
    ResetBasicData.reset();

    RawSql rawSql = RawSqlBuilder.parse("select r.id, r.name from o_customer r ")
      .columnMapping("r.id", "id")
      .columnMapping("r.name", "name").create();

    Query<Customer> query = DB.find(Customer.class);
    query.setRawSql(rawSql);
    query.where().ilike("name", "r%");

    query.fetchQuery("contacts");
    query.filterMany("contacts").gt("lastName", "b");

    List<Customer> list = query.findList();
    assertNotNull(list);

    // check also select count(*)
    LoggedSql.start();
    assertThat(query.findCount()).isEqualTo(list.size());
    List<String>sql = LoggedSql.stop();
    assertThat(sql.get(0)).startsWith("select count(*) from ( select r.id from o_customer r");
    assertThat(sql.get(0)).doesNotContain("order by");
  }

  @Test
  void testWithLimit() {
    ResetBasicData.reset();

    RawSql rawSql = RawSqlBuilder.parse("select r.id, r.phone_number from PHONES r where r.phone_number = :num")
      .columnMapping("r.id", "id")
      .columnMapping("r.phone_number", "phoneNumber").create();

    Query<Phone> query = DB.find(Phone.class);
    query.setRawSql(rawSql);
    query.setParameter("num", "45");
    query.setMaxRows(1);

    LoggedSql.start();
    query.findOne();
    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    if (isLimitOffset()) {
      assertThat(sql.get(0)).contains("select r.id, r.phone_number from PHONES r where r.phone_number = ? limit 1");
    } else if (isAnsiSqlLimit()) {
      assertThat(sql.get(0)).contains("select r.id, r.phone_number from PHONES r where r.phone_number = ? fetch next 1 rows only");
    } else if (isSqlServer()) {
      assertThat(sql.get(0)).contains("select top 1 r.id, r.phone_number from PHONES r where r.phone_number = ?");
    }
  }

  @Test
  void testFindCount_when_idPropertyNotMapped() {
    ResetBasicData.reset();

    // mapping does not include the id column & property
    RawSql rawSql = RawSqlBuilder.parse("select r.name, r.status from o_customer r ")
      .columnMapping("r.name", "name")
      .columnMapping("r.status", "status")
      .create();

    Query<Customer> query = DB.find(Customer.class);
    query.setRawSql(rawSql);
    query.where().ilike("name", "r%").orderBy("name");

    LoggedSql.start();
    List<Customer> list = query.findList();
    assertNotNull(list);

    // check also select count(*)
    assertThat(query.findCount()).isEqualTo(list.size());
    List<String>sql = LoggedSql.stop();
    assertThat(sql.get(0)).startsWith("select r.name, r.status from o_customer r  where lower(r.name) like ?");
    assertThat(sql.get(0)).contains(" order by r.name");
    assertThat(sql.get(1)).startsWith("select count(*) from ( select r.name, r.status from o_customer r  where lower(r.name) like ?");
    assertThat(sql.get(1)).doesNotContain("order by");
  }

  @Test
  @IgnorePlatform({Platform.MYSQL, Platform.MARIADB, Platform.SQLSERVER})
  void test_upperCaseSql() {

    ResetBasicData.reset();

    RawSql rawSql = RawSqlBuilder.parse("select id, NAME from O_CUSTOMER ").create();

    Query<Customer> query = DB.find(Customer.class);
    query.setRawSql(rawSql);
    query.where().ilike("name", "r%");

    List<Customer> list = query.findList();
    assertNotNull(list);
  }

  @Test
  void testFirstRowsMaxRows() throws InterruptedException, ExecutionException {
    ResetBasicData.reset();

    RawSql rawSql =
      RawSqlBuilder
        .parse("select r.id, r.name from o_customer r ")
        .columnMapping("r.id", "id")
        .columnMapping("r.name", "name")
        .create();

    Query<Customer> query = DB.find(Customer.class);
    query.setRawSql(rawSql);

    int initialRowCount = query.findCount();

    query.setFirstRow(1);
    query.setMaxRows(2);
    query.orderBy("id");

    List<Customer> list = query.findList();

    int rowCount = query.findCount();
    FutureRowCount<Customer> futureRowCount = query.findFutureCount();

    assertEquals(initialRowCount, rowCount);
    assertEquals(initialRowCount, futureRowCount.get().intValue());

    // check that lazy loading still executes
    for (Customer customer : list) {
      customer.getCretime();
    }

  }

  @Test
  void testPaging() {
    ResetBasicData.reset();

    RawSql rawSql = RawSqlBuilder.parse("select r.id, r.name from o_customer r ")
      .columnMapping("r.id", "id")
      .columnMapping("r.name", "name")
      .create();

    Query<Customer> query = DB.find(Customer.class);
    query.setRawSql(rawSql);

    int initialRowCount = query.findCount();

    PagedList<Customer> page = query.setMaxRows(2).findPagedList();

    List<Customer> list = page.getList();
    int rowCount = page.getTotalCount();

    assertEquals(2, list.size());
    assertEquals(initialRowCount, rowCount);

    // check that lazy loading executes
    for (Customer customer : list) {
      customer.getCretime();
    }

  }

  @Test
  void testPaging_with_existingRawSqlOrderBy_expect_id_appendToOrderBy() {
    ResetBasicData.reset();

    RawSql rawSql = RawSqlBuilder.parse("select o.id, o.order_date, o.ship_date from o_order o order by o.ship_date desc")
      .columnMapping("o.id", "id")
      .columnMapping("o.order_date", "orderDate")
      .columnMapping("o.ship_date", "shipDate")
      .create();

    Query<Order> query = DB.find(Order.class);
    query.setRawSql(rawSql);

    query.setMaxRows(100);
    query.findList();

    if (isSqlServer()) {
      assertSql(query).contains("top 100 ");
      assertSql(query).contains("order by o.ship_date desc");
    } else if (isOracle() || isDb2()) {
      assertSql(query).contains("fetch next 100 rows only");
    } else {
      assertSql(query).contains("order by o.ship_date desc limit 100");
    }

    // check also select count(*)
    LoggedSql.start();
    query.findCount();
    List<String>sql = LoggedSql.stop();
    assertThat(sql.get(0)).startsWith("select count(*) from ( select o.id from o_order o");
    assertThat(sql.get(0)).doesNotContain("order by");
  }

  @Test
  void testPaging_with_existingRawSqlOrderBy_expect_id_appendToOrderBy_with_id() {
    ResetBasicData.reset();

    RawSql rawSql = RawSqlBuilder.parse("select o.id, o.order_date, o.ship_date from o_order o order by o.ship_date desc")
      .columnMapping("o.id", "id")
      .columnMapping("o.order_date", "orderDate")
      .columnMapping("o.ship_date", "shipDate")
      .create();

    Query<Order> query = DB.find(Order.class);
    query.setRawSql(rawSql);
    query.orderById(true);

    query.setMaxRows(100);
    query.findList();

    if (isSqlServer()) {
      assertSql(query).contains("top 100 ");
      assertSql(query).contains("order by o.ship_date desc, o.id");
    } else if (isOracle() || isDb2()) {
      assertSql(query).contains("fetch next 100 rows only");
    } else {
      assertSql(query).contains("order by o.ship_date desc, o.id limit 100");
    }

    // check also select count(*)
    LoggedSql.start();
    query.findCount();
    List<String>sql = LoggedSql.stop();
    assertThat(sql.get(0)).startsWith("select count(*) from ( select o.id from o_order o");
    assertThat(sql.get(0)).doesNotContain("order by");
  }

  @IgnorePlatform(Platform.ORACLE)
  @Test
  void testPaging_when_setOrderBy_expect_id_appendToOrderBy() {
    ResetBasicData.reset();

    RawSql rawSql = RawSqlBuilder.parse("select o.id, o.order_date, o.ship_date from o_order o order by o.ship_date desc nulls last")
      .columnMapping("o.id", "id")
      .columnMapping("o.order_date", "orderDate")
      .columnMapping("o.ship_date", "shipDate")
      .create();

    Query<Order> query = DB.find(Order.class);
    query.setRawSql(rawSql);

    query.setMaxRows(100);

    if (isSqlServer()) {
      query.orderBy("coalesce(shipDate, getdate()) desc");
      query.findList();

      assertThat(sqlOf(query)).contains("order by coalesce(o.ship_date, getdate()) desc");
      assertThat(sqlOf(query)).contains("select top 100");

    } else {
      query.orderBy("coalesce(shipDate, now()) desc");
      query.findList();

      if (isDb2()) {
        assertSql(query).contains("order by coalesce(o.ship_date, now()) desc fetch next 100 rows only");
      } else {
        assertSql(query).contains("order by coalesce(o.ship_date, now()) desc limit 100");
      }
    }
  }

  @IgnorePlatform(Platform.ORACLE)
  @Test
  void testPaging_when_setOrderBy_expect_id_appendToOrderBy_with_id() {
    ResetBasicData.reset();

    RawSql rawSql = RawSqlBuilder.parse("select o.id, o.order_date, o.ship_date from o_order o order by o.ship_date desc nulls last")
      .columnMapping("o.id", "id")
      .columnMapping("o.order_date", "orderDate")
      .columnMapping("o.ship_date", "shipDate")
      .create();

    Query<Order> query = DB.find(Order.class);
    query.setRawSql(rawSql);

    query.setMaxRows(100);
    query.orderById(true);

    if (isSqlServer()) {
      query.orderBy("coalesce(shipDate, getdate()) desc");
      query.findList();

      assertThat(sqlOf(query)).contains("order by coalesce(o.ship_date, getdate()) desc, o.id");
      assertThat(sqlOf(query)).contains("select top 100");

    } else {
      query.orderBy("coalesce(shipDate, now()) desc");
      query.findList();

      if (isDb2()) {
        assertSql(query).contains("order by coalesce(o.ship_date, now()) desc, o.id fetch next 100 rows only");
      } else {
        assertSql(query).contains("order by coalesce(o.ship_date, now()) desc, o.id limit 100");
      }
    }
  }

  @Test
  void testPaging_when_setOrderBy_containsId_expect_leaveAsIs() {
    ResetBasicData.reset();

    RawSql rawSql = RawSqlBuilder.parse("select o.id, o.order_date, o.ship_date from o_order o order by o.ship_date desc")
      .columnMapping("o.id", "id")
      .columnMapping("o.order_date", "orderDate")
      .columnMapping("o.ship_date", "shipDate")
      .create();

    Query<Order> query = DB.find(Order.class);
    query.setRawSql(rawSql);

    query.setMaxRows(100);
    query.orderBy("id desc");
    PagedList<Order> pagedList = query.findPagedList();
    pagedList.getList();
    pagedList.getTotalCount();

    if (isSqlServer()) {
      assertThat(sqlOf(query)).contains("select top 100 ");
      assertThat(sqlOf(query)).contains("order by o.id desc");
    } else if (isOracle() || isDb2()) {
      assertThat(sqlOf(query)).contains("fetch next 100 rows only");
    } else {
      assertThat(sqlOf(query)).contains("order by o.id desc limit 100");
    }
  }
}
