package org.tests.query;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.ExpressionList;
import io.ebean.Query;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestQueryFilterMany extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    LoggedSql.start();

    Customer customer = DB.find(Customer.class)
      .fetchLazy("orders")
      .filterMany("orders").eq("status", Order.Status.NEW)
      .where().ieq("name", "Rob")
      .orderBy().asc("id").setMaxRows(1)
      .findList().get(0);

    final int size = customer.getOrders().size();
    assertThat(size).isGreaterThan(0);

    List<String> sqlList = LoggedSql.stop();
    assertEquals(2, sqlList.size());
    assertThat(sqlList.get(1)).contains("status = ?");

    // Currently this does not include the query filter
    DB.refreshMany(customer, "orders");

  }

  @Test
  public void filterMany_firstMaxRows_fluidStyle() {

    ResetBasicData.reset();

    LoggedSql.start();

    final Query<Customer> query = DB.find(Customer.class)
      .where().ieq("name", "Rob")
      // fluid style adding maxRows/firstRow to filterMany
      .filterMany("orders").eq("status", Order.Status.NEW).orderBy("id desc").setMaxRows(100).setFirstRow(3)
      .orderBy().asc("id").setMaxRows(5);

    final List<Customer> customers = query.findList();
    assertThat(customers).isNotEmpty();
    List<String> sqlList = LoggedSql.stop();
    assertEquals(2, sqlList.size());
    assertThat(sqlList.get(0)).contains("lower(t0.name) = ?");
    assertThat(sqlList.get(1)).contains("status = ?");

    if (isH2() || isPostgresCompatible()) {
      assertThat(sqlList.get(0)).doesNotContain("offset");
      assertThat(sqlList.get(0)).contains(" limit 5");
      assertThat(sqlList.get(1)).contains(" offset 3");
      assertThat(sqlList.get(1)).contains(" limit 100");
    }
  }

  @Test
  public void test_firstMaxRows() {

    ResetBasicData.reset();

    LoggedSql.start();

    final Query<Customer> query = DB.find(Customer.class)
      .where().ieq("name", "Rob")
      .orderBy().asc("id").setMaxRows(5);

    // non-fluid style adding maxRows/firstRow
    final ExpressionList<Customer> filterMany = query.filterMany("orders").orderBy("id desc").eq("status", Order.Status.NEW);
    filterMany.setMaxRows(100);
    filterMany.setFirstRow(3);

    final List<Customer> customers = query.findList();
    assertThat(customers).isNotEmpty();
    List<String> sqlList = LoggedSql.stop();
    assertEquals(2, sqlList.size());

    assertThat(sqlList.get(0)).contains("lower(t0.name) = ?");
    assertThat(sqlList.get(1)).contains("status = ?");

    if (isH2() || isPostgresCompatible()) {
      assertThat(sqlList.get(0)).doesNotContain("offset");
      assertThat(sqlList.get(0)).contains(" limit 5");
      assertThat(sqlList.get(1)).contains(" offset 3");
      assertThat(sqlList.get(1)).contains(" limit 100");
    }
  }

  @Test
  public void filterMany_firstMaxRows_expressionFluidStyle() {

    ResetBasicData.reset();

    LoggedSql.start();

    final Query<Customer> query = DB.find(Customer.class)
      .where().ieq("name", "Rob")
      // use expression + fluid style adding maxRows/firstRow to filterMany
      .filterMany("orders", "status = ?", Order.Status.NEW)
        .setMaxRows(100).setFirstRow(3).orderBy("orderDate desc, id")
      .orderBy().asc("id").setMaxRows(5);

    final List<Customer> customers = query.findList();
    assertThat(customers).isNotEmpty();
    List<String> sqlList = LoggedSql.stop();
    assertEquals(2, sqlList.size());
    assertThat(sqlList.get(0)).contains("lower(t0.name) = ?");
    assertThat(sqlList.get(1)).contains("t0.status = ?");

    if (isH2() || isPostgresCompatible()) {
      assertThat(sqlList.get(0)).doesNotContain("offset");
      assertThat(sqlList.get(0)).contains(" limit 5");
      assertThat(sqlList.get(1)).contains(" order by t0.order_date desc, t0.id limit 100 offset 3");
    }
  }

  @Test
  public void filterManyRaw_firstMaxRows_expressionFluidStyle() {
    ResetBasicData.reset();

    LoggedSql.start();

    final Query<Customer> query = DB.find(Customer.class)
      .where().ieq("name", "Rob")
      // use expression + fluid style adding maxRows/firstRow to filterMany
      .filterManyRaw("orders", "status = ?", Order.Status.NEW)
      .setMaxRows(100).setFirstRow(3).orderBy("orderDate desc, id")
      .orderBy().asc("id").setMaxRows(5);

    final List<Customer> customers = query.findList();
    assertThat(customers).isNotEmpty();
    List<String> sqlList = LoggedSql.stop();
    assertEquals(2, sqlList.size());
    assertThat(sqlList.get(0)).contains("lower(t0.name) = ?");
    assertThat(sqlList.get(1)).contains("t0.status = ?");

    if (isH2() || isPostgresCompatible()) {
      assertThat(sqlList.get(0)).doesNotContain("offset");
      assertThat(sqlList.get(0)).contains(" limit 5");
      assertThat(sqlList.get(1)).contains(" order by t0.order_date desc, t0.id limit 100 offset 3");
    }
  }

  @Test
  public void filterManyRaw_singleQuery() {
    ResetBasicData.reset();

    LoggedSql.start();

    final Query<Customer> query = DB.find(Customer.class)
      .where().ieq("name", "Rob")
      .filterManyRaw("orders", "status = ?", Order.Status.NEW)
      .orderBy().asc("id");

    final List<Customer> customers = query.findList();
    assertThat(customers).isNotEmpty();
    List<String> sqlList = LoggedSql.stop();
    assertEquals(1, sqlList.size());
    assertThat(sqlList.get(0)).contains(" left join o_customer t2 on t2.id = t1.kcustomer_id and t1.status = ? where ");
    assertThat(sqlList.get(0)).contains(" where lower(t0.name) = ? order by t0.id");
  }

  @Test
  public void test_with_findOne_rawSeparateQuery() {
    ResetBasicData.reset();

    LoggedSql.start();
    Customer customer = DB.find(Customer.class)
      .setMaxRows(1)
      .orderBy().asc("id")
      .fetch("orders")
      .filterMany("orders").raw("orderDate is not null")
      .findOne();

    assertThat(customer).isNotNull();
    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("from o_customer t0 order by");
    assertThat(sql.get(1)).contains("from o_order t0 join o_customer t1 on t1.id = t0.kcustomer_id where");
  }

  @Test
  public void test_with_findOne_rawSameQuery() {
    ResetBasicData.reset();

    LoggedSql.start();
    var result = DB.find(Customer.class)
      .orderBy().asc("id")
      .fetch("orders")
      .filterMany("orders").raw("orderDate is not null")
      .findList();

    assertThat(result).isNotEmpty();
    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("from o_customer t0 left join o_order t1 on t1.kcustomer_id = t0.id and t1.order_date is not null left join o_customer t2 on t2.id = t1.kcustomer_id and t1.order_date is not null order by t0.id");
  }

  @Test
  public void test_with_findOneOrEmpty() {

    ResetBasicData.reset();

    Optional<Customer> customer = DB.find(Customer.class)
      .setMaxRows(1)
      .orderBy().asc("id")
      .fetch("orders")
      .filterMany("orders").raw("1 = 0")
      .findOneOrEmpty();

    assertThat(customer).isPresent();
  }

  @Test
  public void test_filterMany_excludedExplicitly() {
    Clan clan = new Clan();
    ClanQuest quest = new ClanQuest(clan);
    DB.saveAll(clan, quest);

    LoggedSql.start();

    // fetch when there are no buildings at all
    Optional<ClanQuest> result = DB.find(ClanQuest.class)
      .setId(quest.id)
      .fetch("clan", "buildings")
      .filterMany("clan.buildings").eq("type", Building.CAFE)
      .findOneOrEmpty();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains(" left join building t2 on t2.clan_id = t1.id and t2.type = ? where t0.id = ? order by t0.id");
    assertThat(result).isPresent();
    List<Building> emptyBuildings = result.map(r -> r.clan.buildings).orElseThrow();
    assertThat(emptyBuildings).isEmpty();

    // add some buildings
    var b0 = new Building(clan, Building.CAFE, "b0");
    var b1 = new Building(clan, Building.CAFE, "b1");
    var b2 = new Building(clan, Building.HOUSE, "b2");
    DB.saveAll(b0, b1, b2);

    // fetch with some buildings to match the filter many predicate
    Optional<ClanQuest> resultWithBuildings = DB.find(ClanQuest.class)
      .setId(quest.id)
      .fetch("clan", "buildings")
      .filterMany("clan.buildings").eq("type", Building.CAFE)
      .findOneOrEmpty();

    List<Building> someBuildings = resultWithBuildings.map(r -> r.clan.buildings).orElseThrow();
    assertThat(someBuildings).hasSize(2);
    assertThat(someBuildings.stream().map(b -> b.name).collect(Collectors.toList())).contains("b0", "b1");

    // fetch with no matching buildings
    Optional<ClanQuest> resultWithNoMatchingBuildings = DB.find(ClanQuest.class)
      .setId(quest.id)
      .fetch("clan", "buildings")
      .filterMany("clan.buildings").eq("type", Building.STORE)
      .findOneOrEmpty();

    List<Building> noMatchingBuildings = resultWithNoMatchingBuildings.map(r -> r.clan.buildings).orElseThrow();
    assertThat(noMatchingBuildings).isEmpty();
  }

  @Test
  void test_filterMany_with_isNotEmpty() {
    ResetBasicData.reset();

    LoggedSql.start();

    Query<Customer> query = DB.find(Customer.class)
      .setUnmodifiable(true)
      .select("id, status, name")
      .fetch("orders", "status, orderDate, shipDate")
      .filterMany("orders").raw("1 = 0")
      .where().isNotEmpty("orders")
      .query();

    List<Customer> list = query.findList();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains(" from o_customer t0 left join o_order t1 on t1.kcustomer_id = t0.id and t1.order_date is not null and 1 = 0 where exists ");
    assertThat(sql.get(0)).contains("select t0.id, t0.status, t0.name, t1.id, t1.status, t1.order_date, t1.ship_date from o_customer t0 left join o_order t1 on t1.kcustomer_id = t0.id and t1.order_date is not null and 1 = 0 where exists (select 1 from o_order x where x.kcustomer_id = t0.id and x.order_date is not null) order by t0.id; --bind()");
    for (Customer customer : list) {
      assertThat(customer.getOrders()).isEmpty();
    }
  }

  @Test
  public void test_filterMany_in_findCount() {

    ResetBasicData.reset();

    LoggedSql.start();

    Query<Customer> query = DB.find(Customer.class)
      .fetch("orders")
      .filterMany("orders").in("status", Order.Status.NEW)
      .orderBy().asc("id");

    query.findCount();

    List<String> sqlList = LoggedSql.stop();
    assertEquals(1, sqlList.size());
    assertThat(sqlList.get(0)).contains("select count(*) from o_customer");
    assertThat(sqlList.get(0)).doesNotContain("order by");
  }

  @Test
  public void test_filterMany_copy_findList() {

    ResetBasicData.reset();
    LoggedSql.start();

    Query<Customer> query = DB.find(Customer.class)
      .fetch("orders")
      .filterMany("orders").in("status", Order.Status.NEW)
      .orderBy().asc("id");

    query.copy().findList();

    List<String> sqlList = LoggedSql.stop();
    assertEquals(1, sqlList.size());
    assertThat(sqlList.get(0)).contains("left join o_order t1 on t1.kcustomer_id = t0.id and t1.order_date is not null left join o_customer t2 on t2.id = t1.kcustomer_id and t1.status in (?) order by t0.id");
    if (isPostgresCompatible()) {
      assertThat(sqlList.get(0)).contains("left join o_customer t2 on t2.id = t1.kcustomer_id and t1.status = any(?) order by t0.id");
    } else {
      assertThat(sqlList.get(0)).contains("left join o_customer t2 on t2.id = t1.kcustomer_id and t1.status in (?) order by t0.id");
    }
  }

  @Test
  public void test_filterMany_fetchQuery() {

    ResetBasicData.reset();
    LoggedSql.start();

    Query<Customer> query = DB.find(Customer.class)
      .fetchQuery("orders") // explicitly fetch orders separately
      .filterMany("orders").in("status", Order.Status.NEW)
      .orderBy().asc("id");

    query.findList();

    List<String> sqlList = LoggedSql.stop();
    assertEquals(2, sqlList.size());
    assertThat(sqlList.get(0)).contains("from o_customer t0");
    if (isPostgresCompatible()) {
      assertThat(sqlList.get(1)).contains("from o_order t0 join o_customer t1 on t1.id = t0.kcustomer_id where t0.order_date is not null and (t0.kcustomer_id) = any(?)");
      assertThat(sqlList.get(1)).contains(" and t0.status = any(?)");
    } else {
      assertThat(sqlList.get(1)).contains("from o_order t0 join o_customer t1 on t1.id = t0.kcustomer_id where t0.order_date is not null and (t0.kcustomer_id) in ");
      assertThat(sqlList.get(1)).contains(" and t0.status in ");
    }
  }

  @Test
  public void testDisjunction() {

    ResetBasicData.reset();
    LoggedSql.start();

    DB.find(Customer.class)
      .filterMany("orders")
      .or()
      .eq("status", Order.Status.NEW)
      .eq("orderDate", LocalDate.now())
      .findList();

    List<String> sql = LoggedSql.stop();
    assertEquals(1, sql.size());
    assertSql(sql.get(0)).contains(" left join o_customer t2 on t2.id = t1.kcustomer_id and (t1.status = ? or t1.order_date = ?) order by t0.id");
  }

  @Test
  public void testNestedFilterMany() {

    ResetBasicData.reset();
    LoggedSql.start();
    DB.find(Customer.class)
      .filterMany("contacts").isNotNull("firstName")
      .filterMany("contacts.notes").istartsWith("title", "foo")
      .findList();

    List<String> sql = LoggedSql.stop();

    assertThat(sql.size()).isGreaterThan(1);
    assertSql(sql.get(0)).contains(" from o_customer t0 left join contact t1 on t1.customer_id = t0.id and t1.first_name is not null order by t0.id; --bind()");
    platformAssertIn(sql.get(1), " from contact_note t0 where (t0.contact_id)");
    assertSql(sql.get(1)).contains(" and lower(t0.title) like");
  }

  @Test
  public void testFetchAndFilterMany() {

    ResetBasicData.reset();
    LoggedSql.start();
    DB.find(Customer.class)
      .fetch("contacts")
      .filterMany("contacts.notes").istartsWith("title", "foo")
      .findList();

    List<String> sql = LoggedSql.stop();

    assertThat(sql.size()).isEqualTo(2);
    assertSql(sql.get(0)).contains(" from o_customer t0 left join contact t1 on t1.customer_id = t0.id");
    platformAssertIn(sql.get(1), " from contact_note t0 where (t0.contact_id)");
    assertSql(sql.get(1)).contains(" and lower(t0.title) like");
  }

  @Test
  public void testFilterManyUsingExpression() {

    ResetBasicData.reset();
    LoggedSql.start();

    DB.find(Customer.class)
      .where()
      .filterMany("contacts", "firstName isNotNull and email istartsWith ?", "rob")
      .findList();

    List<String> sql = LoggedSql.stop();

    assertThat(sql).hasSize(1);
    if (isSqlServer()) {
      assertSql(sql.get(0)).contains(" from o_customer t0 left join contact t1 on t1.customer_id = t0.id and (t1.first_name is not null and lower(t1.email) like ? order by t0.id; --bind(rob%)");
    } else {
      assertSql(sql.get(0)).contains(" from o_customer t0 left join contact t1 on t1.customer_id = t0.id and (t1.first_name is not null and lower(t1.email) like ? escape'|') order by t0.id; --bind(rob%)");
    }
  }
}
