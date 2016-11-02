package com.avaje.tests.query.aggregation;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.tevent.TEventMany;
import com.avaje.tests.model.tevent.TEventOne;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestAggregationCount extends BaseTestCase {

  @Test
  public void test() {

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


    Query<TEventOne> query = Ebean.find(TEventOne.class)
        .select("name, count, totalUnits, totalAmount")
        .where()
          .startsWith("logs.description", "a")
        .having()
          .ge("count", 1)
        .orderBy().asc("name");

    List<TEventOne> list = query.findList();
    for (TEventOne eventOne : list) {
      System.out.println(eventOne.getId() + " " + eventOne.getName() + " count:" + eventOne.getCount() + " units:" + eventOne.getTotalUnits() + " amount:" + eventOne.getTotalAmount());
    }

    assertThat(list).isNotEmpty();

    String sql = query.getGeneratedSql();
    assertThat(sql).contains("select t0.id c0, t0.name c1, count(u1.*) c2, sum(u1.units) c3, sum(u1.units * u1.amount) c4 from tevent_one t0");
    assertThat(sql).contains("from tevent_one t0 join tevent_many u1 on u1.event_id = t0.id ");
    assertThat(sql).contains("where u1.description like ? ");
    assertThat(sql).contains(" group by t0.id, t0.name having count(u1.*) >= ?  order by t0.name");

  }

}
