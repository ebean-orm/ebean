package org.tests.query.aggregation;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Order;
import org.tests.model.basic.OrderDetail;
import org.tests.model.basic.ResetBasicData;
import org.tests.model.tevent.TEventMany;
import org.tests.model.tevent.TEventOne;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestAggregationCount extends BaseTestCase {

  @BeforeAll
  public static void setup() {
    TEventOne one = new TEventOne("first", TEventOne.Status.AA);
    one.getLogs().add(new TEventMany("all", 1, BigDecimal.valueOf(10)));
    one.getLogs().add(new TEventMany("be", 2, BigDecimal.valueOf(12.2)));
    one.getLogs().add(new TEventMany("add", 3, BigDecimal.valueOf(13)));
    DB.save(one);

    TEventOne two = new TEventOne("second", TEventOne.Status.AA);
    two.getLogs().add(new TEventMany("at", 10, BigDecimal.valueOf(10)));
    two.getLogs().add(new TEventMany("add", 30, BigDecimal.valueOf(13)));
    two.getLogs().add(new TEventMany("alf", 30, BigDecimal.valueOf(13)));
    DB.save(two);

    TEventOne three = new TEventOne("thrird", TEventOne.Status.BB);
    DB.save(three);
    three.setName("third");
    DB.save(three);
  }

  @Test
  public void testBaseSelect() {

    Query<TEventOne> query = DB.find(TEventOne.class);
    List<TEventOne> list = query.findList();

    String sql = sqlOf(query, 5);
    assertThat(sql).contains("select t0.id, t0.name, t0.status, t0.version, t0.event_id from tevent_one t0");

    for (TEventOne eventOne : list) {
      // lazy loading on Aggregation properties
      // is not expected to work at this stage
      BigDecimal totalAmount = eventOne.getTotalAmount();
      assertThat(totalAmount).isNull();
    }
  }

  @Test
  public void example_count() {

    ResetBasicData.reset();

    LoggedSql.start();

    int count =
      DB.find(TEventOne.class)
        .where().isNotNull("name")
        .findCount();

    assertThat(count).isGreaterThan(1);

    List<String> sql = LoggedSql.stop();
    assertSql(sql.get(0)).contains("select count(*) from tevent_one t0 where t0.name is not null");
  }

  @Test
  public void testNonAggregationLazyLoading() {

    Query<TEventOne> query = DB.find(TEventOne.class).select("id");
    List<TEventOne> list = query.findList();

    String sql = sqlOf(query, 5);
    assertThat(sql).contains("select t0.id from tevent_one t0");

    for (TEventOne eventOne : list) {
      String name = eventOne.getName();
      assertThat(name).isNotNull();
    }
  }

  @Test
  public void findCount_withHaving() {
    Query<TEventOne> query = DB.find(TEventOne.class)
      //.select("id, totalUnits")
      .having()
      .ge("totalUnits", 1)
      .query();

    LoggedSql.start();
    int count = query.findCount();
    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("group by t0.id");
    assertThat(count).isGreaterThan(0);
  }

  @Test
  public void testFull() {

    Query<TEventOne> query2 = DB.find(TEventOne.class)
      .select("name, count, totalUnits, totalAmount")
      .where()
      .startsWith("logs.description", "a")
      .having()
      .ge("count", 1)
      .orderBy().asc("name");

    List<TEventOne> list = query2.findList();
    for (TEventOne eventOne : list) {
      System.out.println(eventOne.getId() + " " + eventOne.getName() + " count:" + eventOne.getCount() + " units:" + eventOne.getTotalUnits() + " amount:" + eventOne.getTotalAmount());
    }

    assertThat(list).isNotEmpty();

    String sql = sqlOf(query2, 5);
    assertThat(sql).contains("select t0.id, t0.name, count(u1.id), sum(u1.my_units), sum(u1.my_units * u1.amount) from tevent_one t0");
    assertThat(sql).contains("from tevent_one t0 join tevent_many u1 on u1.event_id = t0.id ");
    assertThat(sql).contains("where u1.description like ");
    assertThat(sql).contains(" group by t0.id, t0.name having count(u1.id) >= ? order by t0.name");

    // invoke lazy loading
    Long version = list.get(0).getVersion();
    assertThat(version).isNotNull();
  }

  @Test
  public void testOrderByTotal() {

    Query<TEventOne> query = DB.find(TEventOne.class)
      .select("name, count, totalUnits, totalAmount")
      .orderBy().asc("totalUnits").orderBy().asc("name");

    List<TEventOne> list = query.findList();
    assertThat(list).isNotEmpty();

    String sql = sqlOf(query, 5);
    if (isH2()) {
      assertThat(sql).contains("select distinct t0.id, t0.name, count(u1.id), sum(u1.my_units), sum(u1.my_units * u1.amount) from tevent_one t0 ");
    } else if (isPostgresCompatible()) {
      assertThat(sql).contains("t0.name, count(u1.id), sum(u1.my_units), sum(u1.my_units * u1.amount) from tevent_one t0 ");
    }
    assertThat(sql).contains("from tevent_one t0 join tevent_many u1 on u1.event_id = t0.id ");
    assertThat(sql).contains(" group by t0.id, t0.name ");
    assertThat(sql).contains(" order by sum(u1.my_units), t0.name");
  }

  @Test
  public void testSelectSome() {

    Query<TEventOne> query0 = DB.find(TEventOne.class)
      .select("name, count, totalUnits");

    query0.findList();
    String sql = sqlOf(query0, 5);
    assertThat(sql).contains("select t0.id, t0.name, count(u1.id), sum(u1.my_units) from tevent_one t0");
    assertThat(sql).contains("group by t0.id, t0.name");
  }

  @Test
  public void testSelectOnly() {

    Query<TEventOne> query0 = DB.find(TEventOne.class)
      .select("name, count, totalUnits, totalAmount");

    query0.findList();
    String sql = sqlOf(query0, 5);
    assertThat(sql).contains("select t0.id, t0.name, count(u1.id), sum(u1.my_units), sum(u1.my_units * u1.amount) from tevent_one t0");
    assertThat(sql).contains("group by t0.id, t0.name");
  }

  @Test
  public void testSelectWhere() {

    Query<TEventOne> query0 = DB.find(TEventOne.class)
      .select("name, count, totalUnits, totalAmount")
      .where().gt("logs.description", "a").query();

    query0.findList();
    String sql = sqlOf(query0, 5);
    assertThat(sql).contains("select t0.id, t0.name, count(u1.id), sum(u1.my_units), sum(u1.my_units * u1.amount) from tevent_one t0");
    assertThat(sql).contains("group by t0.id, t0.name");
  }

  @Test
  public void testSelectHavingOrderBy() {

    Query<TEventOne> query1 = DB.find(TEventOne.class)
      .select("name, count, totalUnits, totalAmount")
      .having().ge("count", 1)
      .orderBy().asc("name");

    query1.findList();
    assertThat(query1.getGeneratedSql()).contains("having count(u1.id) >= ? order by t0.name");
  }

  @Test
  public void testSelectWithFetch() {

    Query<TEventOne> query0 = DB.find(TEventOne.class)
      .select("name, count")
      .fetch("event", "name");

    query0.findList();
    String sql = sqlOf(query0, 5);
    assertThat(sql).contains("select t0.id, t0.name, count(u1.id), t1.id, t1.name from tevent_one t0 left join tevent t1 on t1.id = t0.event_id join tevent_many u1 on u1.event_id = t0.id ");
    assertThat(sql).contains("group by t0.id, t0.name, t1.id, t1.name");
  }

  @Test
  public void testTopLevelAggregation() {

    Query<TEventOne> query0 = DB.find(TEventOne.class)
      .select("status, maxVersion")
      .where().isNotNull("name")
      .having().ge("maxVersion", 1)
      .query();

    List<TEventOne> list = query0.findList();

    String sql = sqlOf(query0, 5);
    assertThat(sql).contains("select t0.status, max(t0.version) from tevent_one t0");
    assertThat(sql).contains("where t0.name is not null");
    assertThat(sql).contains("group by t0.status");
    assertThat(sql).contains("having max(t0.version) >= ?");


    LoggedSql.start();

    for (TEventOne eventOne : list) {
      assertThat(eventOne.getStatus()).isNotNull();
      assertThat(eventOne.getMaxVersion()).isNotNull();

      // bean has no Id, so it is not in Persistence Context, nor Load context
      assertThat(eventOne.getId()).isNull();
      // ... and it will not invoke lazy loading
      assertThat(eventOne.getName()).isNull();
    }

    List<String> lazyLoadSql = LoggedSql.stop();
    assertThat(lazyLoadSql).isEmpty();
  }

  @Test
  public void testDynamicSingleAttributeAggregation() {

    ResetBasicData.reset();

    Query<Order> query0 = DB.find(Order.class)
      .select("max(updtime)")
      .where().eq("status", Order.Status.NEW)
      .query();

    Timestamp maxUpdateTime = query0.findSingleAttribute();
    assertThat(maxUpdateTime).isNotNull();

    String sql = sqlOf(query0, 5);
    assertThat(sql).contains("select max(t0.updtime) from o_order t0");

    Timestamp maxNotNew = DB.find(Order.class)
      .select("max(updtime)")
      .where().ne("status", Order.Status.NEW)
      .findSingleAttribute();

    assertThat(maxNotNew).isNotNull();

  }

  @Test
  public void testDynamicSingleAttributeAggregation_maxInteger() {

    ResetBasicData.reset();

    Query<OrderDetail> query = DB.find(OrderDetail.class)
      .select("max(orderQty)");

    Integer maxOrderQty = query.findSingleAttribute();
    assertThat(maxOrderQty).isGreaterThan(20);

    String sql = sqlOf(query, 5);
    assertThat(sql).contains("select max(t0.order_qty) from o_order_detail t0");
  }

  @Test
  public void testDynamicSingleAttributeAggregation_minInteger() {

    ResetBasicData.reset();

    Query<OrderDetail> query = DB.find(OrderDetail.class)
      .select("min(orderQty)");

    Integer minOrderQty = query.findSingleAttribute();
    assertThat(minOrderQty).isLessThan(10);

    String sql = sqlOf(query, 5);
    assertThat(sql).contains("select min(t0.order_qty) from o_order_detail t0");
  }

  @Test
  public void testDynamicSingleAttributeAggregation_maxString() {

    ResetBasicData.reset();

    Query<Contact> query = DB.find(Contact.class)
      .select("max(lastName)");

    String maxName = query.findSingleAttribute();
    assertThat(maxName).isNotNull();

    String sql = sqlOf(query, 5);
    assertThat(sql).contains("select max(t0.last_name) from contact t0");
  }

  @Test
  public void testDynamicSingleAttributeAggregation_minString() {

    ResetBasicData.reset();

    Query<Contact> query = DB.find(Contact.class)
      .select("min(firstName)");

    String minName = query.findSingleAttribute();
    assertThat(minName).isNotNull();

    String sql = sqlOf(query, 5);
    assertThat(sql).contains("select min(t0.first_name) from contact t0");
  }


  @Test
  public void testDynamicSingleAttributeAggregation_count() {

    ResetBasicData.reset();

    Query<Contact> query = DB.find(Contact.class)
      .select("count(lastName)");

    Long count = query.findSingleAttribute();
    assertThat(count).isNotNull();

    String sql = sqlOf(query, 5);
    assertThat(sql).contains("select count(t0.last_name) from contact t0");
  }

  @Test
  public void testDynamicSingleAttributeAggregation_countDistinct() {

    ResetBasicData.reset();

    Query<Contact> query = DB.find(Contact.class)
      .select("count(distinct lastName)");

    Long count = query.findSingleAttribute();
    assertThat(count).isNotNull();

    String sql = sqlOf(query, 5);
    assertThat(sql).contains("select count(distinct t0.last_name) from contact t0");
  }

  @Test
  public void example() {

    ResetBasicData.reset();

    LoggedSql.start();

    String maxLastName =
      DB.find(Contact.class)
        .select("max(lastName)")
        .where().isNull("phone")
        .findSingleAttribute();

    assertThat(maxLastName).isNotNull();

    List<String> sql = LoggedSql.stop();
    assertSql(sql.get(0)).contains("select max(t0.last_name) from contact t0");
  }

  @Test
  public void example_countDistinct() {

    ResetBasicData.reset();

    LoggedSql.start();

    Long count =
      DB.find(Contact.class)
        .select("count(distinct lastName)")
        .where().isEmpty("notes")
        .findSingleAttribute();

    assertThat(count).isNotNull();

    List<String> sql = LoggedSql.stop();
    assertSql(sql.get(0)).contains("select count(distinct t0.last_name) from contact t0 where not exists (select 1 from contact_note x where x.contact_id = t0.id)");
  }

  @Test
  public void example_nonAggregateFormula() {

    ResetBasicData.reset();

    LoggedSql.start();

    List<String> names =

      DB.find(Contact.class)
        .select(concat("lastName",", ","firstName"))
        .where().isNull("phone")
        .orderBy().asc("lastName")
        .findSingleAttributeList();

    assertThat(names).isNotEmpty();

    List<String> sql = LoggedSql.stop();
    assertSql(sql.get(0)).contains("select " + concat("t0.last_name",", ","t0.first_name") + " from contact t0 where t0.phone is null order by t0.last_name");
  }

  @Test
  public void concat_expectString() {

    ResetBasicData.reset();

    LoggedSql.start();

    List<String> names =

      DB.find(Contact.class)
        .select(concat("updtime",", ","firstName")+"::String")
        .where().isNull("phone")
        .orderBy().asc("lastName")
        .findSingleAttributeList();

    assertThat(names).isNotEmpty();

    List<String> sql = LoggedSql.stop();
    assertSql(sql.get(0)).contains("select " + concat("t0.updtime",", ","t0.first_name") + " from contact t0");
  }

  @Test
  public void explicitCast() {

    ResetBasicData.reset();

    LoggedSql.start();

    Instant instant =

      DB.find(Contact.class)
        .select("max(updtime)::Instant")
        .where().isNull("phone")
        .findSingleAttribute();

    assertThat(instant).isNotNull();

    List<String> sql = LoggedSql.stop();
    assertSql(sql.get(0)).contains("select max(t0.updtime) from contact t0 where t0.phone is null");
  }


  @Test
  public void formula_mapToProperty() {

    ResetBasicData.reset();

    LoggedSql.start();

    List<Contact> contacts =

      DB.find(Contact.class)
        .select("email, " + concat("lastName",", ","firstName") + " as lastName")
        .where().isNull("phone")
        .orderBy().asc("lastName")
        .findList();

    assertThat(contacts).isNotEmpty();

    for (Contact name : contacts) {
      String lastName = name.getLastName();
      assertThat(lastName).contains(", ");
    }

    List<String> sql = LoggedSql.stop();
    assertSql(sql.get(0)).contains("select t0.id, t0.email, " + concat("t0.last_name",", ","t0.first_name") + " lastName from contact t0 where t0.phone is null order by t0.last_name; --bind()");
  }

}
