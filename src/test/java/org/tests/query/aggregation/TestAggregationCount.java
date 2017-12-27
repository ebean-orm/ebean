package org.tests.query.aggregation;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tests.model.tevent.TEventMany;
import org.tests.model.tevent.TEventOne;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestAggregationCount extends BaseTestCase {

  @BeforeClass
  public static void setup() {
    TEventOne one = new TEventOne("first");
    one.getLogs().add(new TEventMany("all", 1, 10));
    one.getLogs().add(new TEventMany("be", 2, 12.2));
    one.getLogs().add(new TEventMany("add", 3, 13));
    Ebean.save(one);

    TEventOne two = new TEventOne("second");
    two.getLogs().add(new TEventMany("at", 10, 10));
    two.getLogs().add(new TEventMany("add", 30, 13));
    two.getLogs().add(new TEventMany("alf", 30, 13));
    Ebean.save(two);
  }

  @Test
  public void testBaseSelect() {

    Query<TEventOne> query = Ebean.find(TEventOne.class);
    List<TEventOne> list = query.findList();

    String sql = sqlOf(query, 5);
    assertThat(sql).contains("select t0.id, t0.name, t0.version, t0.event_id from tevent_one t0");

    for (TEventOne eventOne : list) {
      // lazy loading on Aggregation properties
      // is not expected to work at this stage
      Double totalAmount = eventOne.getTotalAmount();
      assertThat(totalAmount).isNull();
    }
  }

  @Test
  public void testNonAggregationLazyLoading() {

    Query<TEventOne> query = Ebean.find(TEventOne.class).select("id");
    List<TEventOne> list = query.findList();

    String sql = sqlOf(query, 5);
    assertThat(sql).contains("select t0.id from tevent_one t0");

    for (TEventOne eventOne : list) {
      String name = eventOne.getName();
      assertThat(name).isNotNull();
    }
  }

  @Test
  public void testFull() {

    Query<TEventOne> query2 = Ebean.find(TEventOne.class)
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
    assertThat(sql).contains("select t0.id, t0.name, count(u1.id), sum(u1.units), sum(u1.units * u1.amount) from tevent_one t0");
    assertThat(sql).contains("from tevent_one t0 join tevent_many u1 on u1.event_id = t0.id ");
    assertThat(sql).contains("where u1.description like ");
    assertThat(sql).contains(" group by t0.id, t0.name having count(u1.id) >= ?  order by t0.name");

    // invoke lazy loading
    Long version = list.get(0).getVersion();
    assertThat(version).isNotNull();
  }

  @Test
  public void testOrderByTotal() {

    Query<TEventOne> query = Ebean.find(TEventOne.class)
      .select("name, count, totalUnits, totalAmount")
      .orderBy().asc("totalUnits").order().asc("name");

    List<TEventOne> list = query.findList();
    assertThat(list).isNotEmpty();

    String sql = sqlOf(query, 5);
    if (isH2()) {
      assertThat(sql).contains("select distinct t0.id, t0.name, count(u1.id), sum(u1.units), sum(u1.units * u1.amount), sum(u1.units), t0.name from tevent_one t0 ");
    } else if (isPostgres()) {
      assertThat(sql).contains("t0.name, count(u1.id), sum(u1.units), sum(u1.units * u1.amount), sum(u1.units), t0.name from tevent_one t0 ");
    }
    assertThat(sql).contains("from tevent_one t0 join tevent_many u1 on u1.event_id = t0.id ");
    assertThat(sql).contains(" group by t0.id, t0.name ");
    assertThat(sql).contains(" order by sum(u1.units), t0.name");
  }

  @Test
  public void testSelectOnly() {

    Query<TEventOne> query0 = Ebean.find(TEventOne.class)
      .select("name, count, totalUnits, totalAmount");

    query0.findList();
    String sql = sqlOf(query0, 5);
    assertThat(sql).contains("select t0.id, t0.name, count(u1.id), sum(u1.units), sum(u1.units * u1.amount) from tevent_one t0");
    assertThat(sql).contains("group by t0.id, t0.name");
  }

  @Test
  public void testSelectWhere() {

    Query<TEventOne> query0 = Ebean.find(TEventOne.class)
      .select("name, count, totalUnits, totalAmount")
      .where().gt("logs.description", "a").query();

    query0.findList();
    String sql = sqlOf(query0, 5);
    assertThat(sql).contains("select t0.id, t0.name, count(u1.id), sum(u1.units), sum(u1.units * u1.amount) from tevent_one t0");
    assertThat(sql).contains("group by t0.id, t0.name");
  }

  @Test
  public void testSelectHavingOrderBy() {

    Query<TEventOne> query1 = Ebean.find(TEventOne.class)
      .select("name, count, totalUnits, totalAmount")
      .having().ge("count", 1)
      .orderBy().asc("name");

    query1.findList();
    assertThat(query1.getGeneratedSql()).contains("having count(u1.id) >= ?  order by t0.name");
  }

  @Test
  public void testSelectWithFetch() {

    Query<TEventOne> query0 = Ebean.find(TEventOne.class)
      .select("name, count")
      .fetch("event", "name");

    query0.findList();
    String sql = sqlOf(query0, 5);
    assertThat(sql).contains("select t0.id, t0.name, count(u1.id), t1.id, t1.name from tevent_one t0 left join tevent t1 on t1.id = t0.event_id  join tevent_many u1 on u1.event_id = t0.id ");
    assertThat(sql).contains("group by t0.id, t0.name, t1.id, t1.name");
  }
}
